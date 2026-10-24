package me.harukiyu.clarityvoice.web;

import io.javalin.websocket.WsContext;
import me.harukiyu.clarityvoice.audio.WebEntityAudioSender;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Represents an active WebSocket session belonging to an authenticated Minecraft player.
 */
public class WebVoiceSession {

    private final UUID playerUuid;
    private final String playerName;
    private final WsContext wsContext;
    private final WebEntityAudioSender audioSender;
    private final long connectedAt;

    private volatile boolean muted = false;
    private volatile boolean deafened = false;

    public WebVoiceSession(UUID playerUuid, String playerName, WsContext wsContext, WebEntityAudioSender audioSender) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.wsContext = wsContext;
        this.audioSender = audioSender;
        this.connectedAt = System.currentTimeMillis();
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public WsContext getWsContext() {
        return wsContext;
    }

    public WebEntityAudioSender getAudioSender() {
        return audioSender;
    }

    public long getConnectedAt() {
        return connectedAt;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isDeafened() {
        return deafened;
    }

    public void setDeafened(boolean deafened) {
        this.deafened = deafened;
    }

    public boolean isOpen() {
        return wsContext != null && wsContext.session.isOpen();
    }

    /**
     * Sends binary data (such as spatial audio packets) to the web browser.
     */
    public void sendBinary(byte[] data) {
        if (!isOpen() || deafened) return;
        try {
            wsContext.send(ByteBuffer.wrap(data));
        } catch (Exception ignored) {}
    }

    /**
     * Sends a text / JSON message (such as position updates) to the web browser.
     */
    public void sendText(String message) {
        if (!isOpen()) return;
        try {
            wsContext.send(message);
        } catch (Exception ignored) {}
    }

    /**
     * Closes the WebSocket connection with a status code and reason.
     */
    public void close(int code, String reason) {
        if (wsContext != null && wsContext.session.isOpen()) {
            try {
                wsContext.session.close(code, reason);
            } catch (Exception ignored) {}
        }
        if (audioSender != null) {
            audioSender.close();
        }
    }
}
