package me.harukiyu.clarityvoice;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import me.harukiyu.clarityvoice.command.VoiceCommand;
import me.harukiyu.clarityvoice.config.VoiceConfig;
import me.harukiyu.clarityvoice.listeners.PlayerListener;
import me.harukiyu.clarityvoice.service.OtpManager;
import me.harukiyu.clarityvoice.service.PositionCache;
import me.harukiyu.clarityvoice.service.PositionSyncTask;
import me.harukiyu.clarityvoice.service.SessionManager;
import me.harukiyu.clarityvoice.service.TranslationService;
import me.harukiyu.clarityvoice.voice.ClarityVoiceChatPlugin;
import me.harukiyu.clarityvoice.web.WebVoiceServer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ClarityVoice extends JavaPlugin {

    private static ClarityVoice instance;

    private VoiceConfig voiceConfig;
    private OtpManager otpManager;
    private PositionCache positionCache;
    private SessionManager sessionManager;
    private ClarityVoiceChatPlugin voiceChatPlugin;
    private WebVoiceServer webVoiceServer;
    private PositionSyncTask positionSyncTask;

    public static ClarityVoice getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // 1. Configuration
        saveDefaultConfig();
        this.voiceConfig = new VoiceConfig(getConfig());

        // 2. Services
        this.otpManager = new OtpManager(voiceConfig.getOtpDurationSeconds());
        this.positionCache = new PositionCache();
        this.sessionManager = new SessionManager(voiceConfig);
        TranslationService translationService = new TranslationService(this);

        // 3. Simple Voice Chat API Integration
        this.voiceChatPlugin = new ClarityVoiceChatPlugin(voiceConfig, positionCache, sessionManager);
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(voiceChatPlugin);
            getLogger().info("Registered ClarityVoiceChatPlugin into BukkitVoicechatService.");
        } else {
            getLogger().warning("BukkitVoicechatService not loaded yet. Voice chat bridge will wait for service registration.");
        }

        // 4. Web & WebSocket Server
        this.webVoiceServer = new WebVoiceServer(
            voiceConfig,
            otpManager,
            sessionManager,
            positionCache,
            voiceChatPlugin
        );
        this.webVoiceServer.start();

        // 5. Position Synchronization Bukkit Task
        this.positionSyncTask = new PositionSyncTask(positionCache, sessionManager, voiceConfig, voiceChatPlugin);
        this.positionSyncTask.runTaskTimer(this, 10L, voiceConfig.getPositionSyncIntervalTicks());

        // 6. Event Listeners
        getServer().getPluginManager().registerEvents(
            new PlayerListener(otpManager, sessionManager, positionCache),
            this
        );

        // 7. Commands & Tab Completers
        VoiceCommand voiceCommand = new VoiceCommand(otpManager, sessionManager, voiceChatPlugin, voiceConfig, translationService);
        PluginCommand cmd = getCommand("clarityvoice");
        if (cmd != null) {
            cmd.setExecutor(voiceCommand);
            cmd.setTabCompleter(voiceCommand);
        }

        getLogger().info("=================================================");
        getLogger().log(java.util.logging.Level.INFO, " ClarityVoice v{0} has been ENABLED!", getPluginMeta().getVersion());
        getLogger().log(java.util.logging.Level.INFO, " Web Interface: {0}", voiceConfig.getPublicUrl());
        getLogger().log(java.util.logging.Level.INFO, " Max Distance: {0} blocks", voiceConfig.getMaxDistance());
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ClarityVoice...");

        // 1. Cancel Bukkit Tasks
        if (positionSyncTask != null) {
            try {
                positionSyncTask.cancel();
            } catch (IllegalStateException ignored) {}
        }

        // 2. Stop Web Server
        if (webVoiceServer != null) {
            webVoiceServer.stop();
        }

        // 3. Clean up active sessions and decoders
        if (sessionManager != null) {
            sessionManager.closeAll();
        }

        // 4. Reset Voicechat Plugin
        this.voiceChatPlugin = null;

        getLogger().info("ClarityVoice has been DISABLED.");
    }

    public VoiceConfig getVoiceConfig() {
        return voiceConfig;
    }

    public OtpManager getOtpManager() {
        return otpManager;
    }

    public PositionCache getPositionCache() {
        return positionCache;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ClarityVoiceChatPlugin getVoiceChatPlugin() {
        return voiceChatPlugin;
    }

    public WebVoiceServer getWebVoiceServer() {
        return webVoiceServer;
    }
}
