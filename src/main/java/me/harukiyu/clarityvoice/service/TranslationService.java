package me.harukiyu.clarityvoice.service;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing message translations, hooking into ClarityLangCore when available,
 * or providing a clean native MiniMessage fallback.
 */
public class TranslationService {

    private static final Logger LOGGER = Logger.getLogger(TranslationService.class.getName());
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Plugin plugin;
    private boolean langCoreAvailable = false;

    public TranslationService(Plugin plugin) {
        this.plugin = plugin;
        initLangCore();
    }

    private void initLangCore() {
        if (Bukkit.getPluginManager().isPluginEnabled("ClarityLangCore")) {
            try {
                ClarityLangAPI.registerBundle(plugin, "voice", "lang");
                langCoreAvailable = true;
                LOGGER.info("Successfully hooked into ClarityLangCore with 9 language bundles.");
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Failed to register ClarityVoice bundle with ClarityLangCore: " + t.getMessage());
                langCoreAvailable = false;
            }
        } else {
            LOGGER.info("ClarityLangCore not found. Running with built-in clean translation fallback.");
            langCoreAvailable = false;
        }
    }

    /**
     * Gets a localized component for a player.
     *
     * @param subKey    relative key under voice (e.g. "command.otp.title")
     * @param player    the player
     * @param resolvers MiniMessage tag resolvers
     * @param fallback  fallback MiniMessage string if ClarityLangCore is unavailable
     * @return Adventure Component
     */
    public Component getComponent(String subKey, Player player, String fallback, TagResolver... resolvers) {
        if (langCoreAvailable && player != null) {
            try {
                return ClarityLangAPI.getComponent("voice:" + subKey, player, resolvers);
            } catch (Throwable ignored) {}
        }
        return MINI_MESSAGE.deserialize(fallback, resolvers);
    }

    /**
     * Sends a localized message to a player.
     */
    public void sendMessage(Player player, String subKey, String fallback, TagResolver... resolvers) {
        if (player == null) return;
        player.sendMessage(getComponent(subKey, player, fallback, resolvers));
    }

    public boolean isLangCoreAvailable() {
        return langCoreAvailable;
    }
}
