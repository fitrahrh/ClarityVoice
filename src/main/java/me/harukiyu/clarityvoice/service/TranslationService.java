package me.harukiyu.clarityvoice.service;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing message translations, hooking into ClarityLangCore when available,
 * with full fallback bundles and client locale auto-detection.
 */
public class TranslationService {

    private static final Logger LOGGER = Logger.getLogger(TranslationService.class.getName());
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final String NAMESPACE = "voice";

    public static final List<String> SUPPORTED_LANGS = List.of(
        "en_US", "id_ID", "pt_BR", "es_ES", "zh_CN", "ja_JP", "de_DE", "fr_FR", "ru_RU"
    );

    private final Plugin plugin;
    private boolean langCoreAvailable = false;
    private final Map<String, YamlConfiguration> fallbackBundles = new ConcurrentHashMap<>();
    private final Map<String, YamlConfiguration> jarBundles = new ConcurrentHashMap<>();

    public TranslationService(Plugin plugin) {
        this.plugin = plugin;
        initBundles();
        initLangCore();
    }

    private void initBundles() {
        fallbackBundles.clear();
        jarBundles.clear();

        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        for (String lang : SUPPORTED_LANGS) {
            String lowerLang = lang.toLowerCase(Locale.ROOT);
            File langFile = new File(langFolder, lang + ".yml");

            if (!langFile.exists()) {
                try {
                    plugin.saveResource("languages/" + lang + ".yml", false);
                } catch (Throwable ignored) {}
            }

            YamlConfiguration jarConfig = null;
            try (InputStream stream = plugin.getResource("languages/" + lang + ".yml")) {
                if (stream != null) {
                    jarConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    jarBundles.put(lowerLang, jarConfig);
                }
            } catch (IOException ignored) {}

            YamlConfiguration config = null;
            if (langFile.exists()) {
                config = YamlConfiguration.loadConfiguration(langFile);
                if (jarConfig != null) {
                    boolean modified = false;
                    for (String key : jarConfig.getKeys(true)) {
                        if (!config.contains(key)) {
                            config.set(key, jarConfig.get(key));
                            modified = true;
                        }
                    }
                    if (modified) {
                        try {
                            config.save(langFile);
                        } catch (IOException ignored) {}
                    }
                    config.setDefaults(jarConfig);
                }
            } else if (jarConfig != null) {
                config = jarConfig;
            }

            if (config != null) {
                fallbackBundles.put(lowerLang, config);
            }
        }
    }

    private void initLangCore() {
        if (Bukkit.getPluginManager().isPluginEnabled("ClarityLangCore")) {
            try {
                ClarityLangAPI.registerBundle(plugin, NAMESPACE, "languages");

                File langFolder = new File(plugin.getDataFolder(), "languages");
                if (langFolder.exists()) {
                    try {
                        ClarityLangAPI.registerBundle(NAMESPACE, langFolder);
                    } catch (Throwable ignored) {}
                }

                langCoreAvailable = true;
                LOGGER.info("Successfully hooked into ClarityLangCore for bundle [voice].");
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Failed to register ClarityVoice bundle with ClarityLangCore: {0}", t.getMessage());
                langCoreAvailable = false;
            }
        } else {
            LOGGER.info("ClarityLangCore not found. Running with local multi-language bundles.");
            langCoreAvailable = false;
        }
    }

    @NotNull
    public String getEffectiveLanguageCode(@Nullable CommandSender sender) {
        if (sender instanceof Player player) {
            if (langCoreAvailable) {
                try {
                    String code = ClarityLangAPI.getPlayerLanguageCode(player);
                    if (!code.isBlank()) return code;
                } catch (Throwable ignored) {}
            }
            try {
                Locale clientLocale = player.locale();
                String lang = clientLocale.getLanguage().toLowerCase(Locale.ROOT);
                for (String supported : SUPPORTED_LANGS) {
                    if (supported.toLowerCase(Locale.ROOT).startsWith(lang)) return supported;
                }
            } catch (Throwable ignored) {}
        }
        return "id_ID";
    }

    /**
     * Gets a localized component for a player, trying ClarityLangCore first,
     * then local language bundle, and finally hardcoded fallback.
     */
    @NotNull
    public Component getComponent(@NotNull String key, @Nullable Player player, @NotNull String fallback, @NotNull TagResolver... resolvers) {
        String langCode = getEffectiveLanguageCode(player);
        TagResolver[] enriched = buildResolversWithPrefix(langCode, resolvers);
        String cleanKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        String fullKey = key.contains(":") ? key : NAMESPACE + ":" + cleanKey;

        if (langCoreAvailable) {
            Component coreComp = tryClarityLangCore(fullKey, cleanKey, player, langCode, enriched);
            if (coreComp != null) {
                return coreComp;
            }
        }

        return getFallbackComponent(cleanKey, langCode, fallback, enriched);
    }

    @Nullable
    private Component tryClarityLangCore(String fullKey, String cleanKey, @Nullable Player player, String langCode, TagResolver[] resolvers) {
        try {
            Component comp = player != null
                ? ClarityLangAPI.getComponent(fullKey, player, resolvers)
                : ClarityLangAPI.getComponent(fullKey, langCode, resolvers);

            if (comp instanceof TextComponent tc) {
                String content = tc.content();
                // Check if ClarityLangCore failed to find the key and returned raw placeholder
                if (content.equals("<" + fullKey + ">") || content.equals("<" + cleanKey + ">")
                    || content.equals(fullKey) || content.equals(cleanKey)) {
                    return null;
                }
            }
            return comp;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @NotNull
    private Component getFallbackComponent(@NotNull String cleanKey, @NotNull String langCode, @NotNull String fallback, @NotNull TagResolver... resolvers) {
        String raw = resolveRawKey(cleanKey, langCode);
        if (raw == null) {
            raw = fallback;
        }
        return MINI_MESSAGE.deserialize(raw, resolvers);
    }

    @Nullable
    private String resolveRawKey(@NotNull String cleanKey, @NotNull String langCode) {
        String lowerLang = langCode.toLowerCase(Locale.ROOT);

        // 1. Try disk fallback bundle for requested lang
        String raw = getRawFromConfig(fallbackBundles.get(lowerLang), cleanKey);
        if (raw != null) return raw;

        // 2. Try in-memory jar bundle for requested lang
        raw = getRawFromConfig(jarBundles.get(lowerLang), cleanKey);
        if (raw != null) return raw;

        // 3. Try disk fallback bundle for id_ID
        raw = getRawFromConfig(fallbackBundles.get("id_id"), cleanKey);
        if (raw != null) return raw;

        // 4. Try in-memory jar bundle for id_ID
        raw = getRawFromConfig(jarBundles.get("id_id"), cleanKey);
        if (raw != null) return raw;

        // 5. Try en_US
        raw = getRawFromConfig(fallbackBundles.get("en_us"), cleanKey);
        if (raw != null) return raw;

        return getRawFromConfig(jarBundles.get("en_us"), cleanKey);
    }

    @Nullable
    private String getRawFromConfig(@Nullable YamlConfiguration config, @NotNull String cleanKey) {
        if (config == null) return null;
        return config.getString(cleanKey);
    }

    @NotNull
    private TagResolver[] buildResolversWithPrefix(String langCode, @NotNull TagResolver... resolvers) {
        String prefix = resolveRawKey("prefix", langCode);
        if (prefix == null) prefix = "<aqua>Clarity Voice</aqua>";
        List<TagResolver> list = new ArrayList<>(resolvers.length + 1);
        list.add(Placeholder.parsed("prefix", prefix));
        Collections.addAll(list, resolvers);
        return list.toArray(TagResolver[]::new);
    }

    /**
     * Sends a localized message to a player.
     */
    public void sendMessage(@Nullable Player player, @NotNull String subKey, @NotNull String fallback, @NotNull TagResolver... resolvers) {
        if (player == null) return;
        player.sendMessage(getComponent(subKey, player, fallback, resolvers));
    }

    public boolean isLangCoreAvailable() {
        return langCoreAvailable;
    }
}
