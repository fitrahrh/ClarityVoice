package me.harukiyu.clarityvoice.service;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import io.javalin.websocket.WsContext;
import me.harukiyu.clarityvoice.audio.WebEntityAudioSender;
import me.harukiyu.clarityvoice.config.VoiceConfig;
import me.harukiyu.clarityvoice.web.WebVoiceSession;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages active Web Client voice sessions, single-session-per-player enforcement,
 * and audio encoders/decoders lifecycle.
 */
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());

    private final Map<UUID, WebVoiceSession> sessionsByUuid = new ConcurrentHashMap<>();
    private final Map<String, UUID> wsSessionIdToUuid = new ConcurrentHashMap<>();
    private final Map<UUID, OpusDecoder> modPlayerDecoders = new ConcurrentHashMap<>();

    private final VoiceConfig config;
    private volatile VoicechatServerApi voiceApi;

    public SessionManager(VoiceConfig config) {
        this.config = config;
    }

    public SessionManager(VoiceConfig config, Logger logger) {
        this.config = config;
    }

    public void setVoiceApi(VoicechatServerApi voiceApi) {
        this.voiceApi = voiceApi;
    }

    public VoicechatServerApi getVoiceApi() {
        return voiceApi;
    }

    /**
     * Registers a newly authenticated WebSocket session for a player.
     * Enforces single active session rule by terminating any existing session.
     */
    public synchronized WebVoiceSession registerSession(Player player, WsContext ctx, VoicechatServerApi currentApi) {
        if (currentApi != null) {
            this.voiceApi = currentApi;
        }
        UUID playerUuid = player.getUniqueId();

        // 1. Close existing session if present
        WebVoiceSession existing = sessionsByUuid.remove(playerUuid);
        if (existing != null) {
            existing.close(4003, "Sesi digantikan oleh koneksi baru.");
            if (existing.getWsContext() != null) {
                wsSessionIdToUuid.remove(existing.getWsContext().sessionId());
            }
        }

        // 2. Setup EntityAudioChannel and OpusEncoder if Voicechat API is available
        WebEntityAudioSender audioSender = null;
        if (this.voiceApi != null) {
            try {
                EntityAudioChannel channel = this.voiceApi.createEntityAudioChannel(
                    UUID.randomUUID(),
                    this.voiceApi.fromEntity(player)
                );
                if (channel != null) {
                    channel.setDistance((float) config.getMaxDistance());
                    channel.setCategory(config.getCategoryName());
                }

                OpusEncoder encoder = this.voiceApi.createEncoder();
                audioSender = new WebEntityAudioSender(playerUuid, channel, encoder, this.voiceApi, config.getCategoryName());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to initialize voice channel for web player {0}: {1}", new Object[]{player.getName(), e.getMessage()});
            }
        }

        // 3. Create and register session
        WebVoiceSession session = new WebVoiceSession(playerUuid, player.getName(), ctx, audioSender);
        sessionsByUuid.put(playerUuid, session);
        if (ctx != null) {
            wsSessionIdToUuid.put(ctx.sessionId(), playerUuid);
        }

        // Activate voicechat in-game speaker icon for web players
        updateVoicechatConnectionState(playerUuid, true, false);

        LOGGER.log(Level.INFO, "Player {0} ({1}) connected to Web Voice.", new Object[]{player.getName(), playerUuid});
        return session;
    }

    /**
     * Unregisters a WebSocket session when closed.
     */
    public synchronized void unregisterSession(WsContext ctx) {
        if (ctx == null) return;
        UUID playerUuid = wsSessionIdToUuid.remove(ctx.sessionId());
        if (playerUuid != null) {
            WebVoiceSession session = sessionsByUuid.remove(playerUuid);
            if (session != null) {
                session.close(1000, "Disconnected");
                updateVoicechatConnectionState(playerUuid, false, true);
                LOGGER.log(Level.INFO, "Player {0} disconnected from Web Voice.", session.getPlayerName());
            }
        }
    }

    /**
     * Disconnects a player's active web voice session.
     */
    public synchronized boolean disconnect(UUID playerUuid, String reason) {
        WebVoiceSession session = sessionsByUuid.remove(playerUuid);
        if (session != null) {
            if (session.getWsContext() != null) {
                wsSessionIdToUuid.remove(session.getWsContext().sessionId());
            }
            session.close(1000, reason);
            updateVoicechatConnectionState(playerUuid, false, true);
            LOGGER.log(Level.INFO, "Player {0} was disconnected: {1}", new Object[]{session.getPlayerName(), reason});
            return true;
        }
        return false;
    }

    /**
     * Toggles or updates the in-game speaker icon state in Simple Voice Chat.
     */
    public void updateVoicechatConnectionState(UUID playerUuid, boolean connected, boolean disabled) {
        if (voiceApi == null || playerUuid == null) return;
        try {
            VoicechatConnection conn = voiceApi.getConnectionOf(playerUuid);
            if (conn != null && !conn.isInstalled()) {
                conn.setConnected(connected);
                conn.setDisabled(disabled);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not update in-game voice icon for {0}: {1}", new Object[]{playerUuid, e.getMessage()});
        }
    }

    public WebVoiceSession getSession(UUID playerUuid) {
        return sessionsByUuid.get(playerUuid);
    }

    public WebVoiceSession getSessionByContext(WsContext ctx) {
        if (ctx == null) return null;
        UUID uuid = wsSessionIdToUuid.get(ctx.sessionId());
        return uuid != null ? sessionsByUuid.get(uuid) : null;
    }

    public boolean isWebConnected(UUID playerUuid) {
        WebVoiceSession session = sessionsByUuid.get(playerUuid);
        return session != null && session.isOpen();
    }

    public Collection<WebVoiceSession> getAllSessions() {
        return sessionsByUuid.values();
    }

    public int getActiveSessionCount() {
        return sessionsByUuid.size();
    }

    /**
     * Obtains or creates an OpusDecoder for a mod player to maintain Opus codec state.
     */
    public OpusDecoder getOrCreateDecoder(UUID senderUuid, VoicechatServerApi currentApi) {
        VoicechatServerApi api = (currentApi != null) ? currentApi : this.voiceApi;
        if (api == null) return null;
        return modPlayerDecoders.computeIfAbsent(senderUuid, id -> {
            try {
                return api.createDecoder();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create OpusDecoder for {0}: {1}", new Object[]{senderUuid, e.getMessage()});
                return null;
            }
        });
    }

    /**
     * Cleans up all state and decoders when a player leaves the server.
     */
    public synchronized void cleanupPlayer(UUID playerUuid) {
        disconnect(playerUuid, "Player quit the game.");
        updateVoicechatConnectionState(playerUuid, false, true);
        OpusDecoder decoder = modPlayerDecoders.remove(playerUuid);
        if (decoder != null && !decoder.isClosed()) {
            try {
                decoder.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Closes all active sessions and decoders when the plugin disables.
     */
    public synchronized void closeAll() {
        for (WebVoiceSession session : sessionsByUuid.values()) {
            updateVoicechatConnectionState(session.getPlayerUuid(), false, true);
            session.close(1001, "Server shutting down.");
        }
        sessionsByUuid.clear();
        wsSessionIdToUuid.clear();

        for (OpusDecoder decoder : modPlayerDecoders.values()) {
            if (decoder != null && !decoder.isClosed()) {
                try {
                    decoder.close();
                } catch (Exception ignored) {}
            }
        }
        modPlayerDecoders.clear();
    }
}
