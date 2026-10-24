package me.harukiyu.clarityvoice.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsBinaryMessageContext;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import me.harukiyu.clarityvoice.ClarityVoice;
import me.harukiyu.clarityvoice.audio.AudioPayloadHelper;
import me.harukiyu.clarityvoice.config.VoiceConfig;
import me.harukiyu.clarityvoice.model.CachedPlayerLocation;
import me.harukiyu.clarityvoice.service.OtpManager;
import me.harukiyu.clarityvoice.service.PositionCache;
import me.harukiyu.clarityvoice.service.SessionManager;
import me.harukiyu.clarityvoice.voice.ClarityVoiceChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded Javalin Web & WebSocket server providing static files for the Web Client
 * and managing high-performance WebSocket audio streaming on endpoint /ws/voice.
 */
public class WebVoiceServer {

    private static final Logger LOGGER = Logger.getLogger(WebVoiceServer.class.getName());

    private final VoiceConfig config;
    private final OtpManager otpManager;
    private final SessionManager sessionManager;
    private final PositionCache positionCache;
    private final ClarityVoiceChatPlugin voicePlugin;
    private final Gson gson = new Gson();

    private Javalin app;

    public WebVoiceServer(
        VoiceConfig config,
        OtpManager otpManager,
        SessionManager sessionManager,
        PositionCache positionCache,
        ClarityVoiceChatPlugin voicePlugin
    ) {
        this.config = config;
        this.otpManager = otpManager;
        this.sessionManager = sessionManager;
        this.positionCache = positionCache;
        this.voicePlugin = voicePlugin;
    }

    public WebVoiceServer(
        VoiceConfig config,
        OtpManager otpManager,
        SessionManager sessionManager,
        PositionCache positionCache,
        ClarityVoiceChatPlugin voicePlugin,
        Logger logger
    ) {
        this(config, otpManager, sessionManager, positionCache, voicePlugin);
    }

    /**
     * Starts the Javalin web and websocket server asynchronously.
     */
    public void start() {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Swap thread context classloader to plugin classloader for Jetty in Paper
            Thread.currentThread().setContextClassLoader(ClarityVoice.class.getClassLoader());

            app = Javalin.create(javalinConfig -> {
                // Static file serving from resources/web at root "/"
                javalinConfig.staticFiles.add(staticFiles -> {
                    staticFiles.hostedPath = "/";
                    staticFiles.directory = "/web";
                    staticFiles.location = Location.CLASSPATH;
                });

                javalinConfig.showJavalinBanner = false;
            });

            // WebSocket route for Voice Chat
            app.ws("/ws/voice", ws -> {
                ws.onConnect(this::handleConnect);
                ws.onBinaryMessage(this::handleBinaryMessage);
                ws.onMessage(this::handleTextMessage);
                ws.onClose(this::handleClose);
                ws.onError(this::handleError);
            });

            // REST status endpoint
            app.get("/api/status", ctx -> {
                Map<String, Object> status = new LinkedHashMap<>();
                status.put("plugin", "ClarityVoice");
                status.put("version", "1.0.0");
                status.put("activeWebSessions", sessionManager.getActiveSessionCount());
                status.put("voiceChatModConnected", voicePlugin.getServerApi() != null);
                ctx.json(status);
            });

            app.start(config.getHost(), config.getPort());
            LOGGER.log(Level.INFO, "Web Voice Server listening on {0}:{1,number,#}", new Object[]{config.getHost(), config.getPort()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start Web Voice Server on port " + config.getPort(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private void handleConnect(WsConnectContext ctx) {
        String otp = ctx.queryParam("otp");
        if (otp == null || otp.isBlank()) {
            ctx.send("{\"type\":\"error\",\"message\":\"Kode OTP tidak disertakan.\"}");
            ctx.session.close(4001, "Missing OTP");
            return;
        }

        UUID playerUuid = otpManager.validateAndConsume(otp);
        if (playerUuid == null) {
            ctx.send("{\"type\":\"error\",\"message\":\"Kode OTP tidak valid atau telah kedaluwarsa.\"}");
            ctx.session.close(4001, "Invalid or expired OTP");
            return;
        }

        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            ctx.send("{\"type\":\"error\",\"message\":\"Pemain tidak sedang online di server Minecraft.\"}");
            ctx.session.close(4002, "Player not online");
            return;
        }

        WebVoiceSession session = sessionManager.registerSession(player, ctx, voicePlugin.getServerApi());

        // Send auth success packet
        Map<String, Object> welcome = new LinkedHashMap<>();
        welcome.put("type", "auth_success");
        welcome.put("uuid", playerUuid.toString());
        welcome.put("playerName", player.getName());
        welcome.put("maxDistance", config.getMaxDistance());
        session.sendText(gson.toJson(welcome));
    }

    /**
     * Handles microphone audio received from browser (Web -> Mod and Web -> Web).
     */
    private void handleBinaryMessage(WsBinaryMessageContext ctx) {
        WebVoiceSession session = sessionManager.getSessionByContext(ctx);
        if (session == null || session.isMuted() || session.isDeafened()) {
            return;
        }

        byte[] rawBytes = ctx.data();
        short[] pcm = AudioPayloadHelper.extractPcmSamples(rawBytes);
        if (pcm == null || pcm.length == 0) {
            return;
        }

        VoicechatServerApi voiceApi = voicePlugin.getServerApi();
        de.maxhenkel.voicechat.api.VoicechatConnection senderConn = voiceApi != null ? voiceApi.getConnectionOf(session.getPlayerUuid()) : null;
        de.maxhenkel.voicechat.api.Group group = (senderConn != null && senderConn.isInGroup()) ? senderConn.getGroup() : null;
        boolean inGroup = group != null;

        // 1. Send to Simple Voice Chat mod players via EntityAudioChannel & StaticAudioChannel
        if (session.getAudioSender() != null) {
            session.getAudioSender().sendAudio(pcm, inGroup, group);
        }

        // 2. Broadcast to other Web clients
        CachedPlayerLocation senderLoc = positionCache.get(session.getPlayerUuid());
        double maxDist = config.getMaxDistance();
        double maxDistSq = maxDist * maxDist;

        byte[] groupPacket = null;
        byte[] proximityPacket = null;
        boolean allowProximity = !inGroup || (group != null && group.getType() != de.maxhenkel.voicechat.api.Group.Type.ISOLATED);

        for (WebVoiceSession other : sessionManager.getAllSessions()) {
            if (other.getPlayerUuid().equals(session.getPlayerUuid())) continue;
            if (other.isDeafened()) continue;

            // Group check
            if (inGroup && group != null && voiceApi != null) {
                de.maxhenkel.voicechat.api.VoicechatConnection otherConn = voiceApi.getConnectionOf(other.getPlayerUuid());
                if (otherConn != null && otherConn.isInGroup()) {
                    de.maxhenkel.voicechat.api.Group otherGroup = otherConn.getGroup();
                    if (otherGroup != null && group.getId() != null && group.getId().equals(otherGroup.getId())) {
                        if (groupPacket == null) {
                            groupPacket = AudioPayloadHelper.createGroupAudioPacket(session.getPlayerUuid(), group.getId(), pcm);
                        }
                        other.sendBinary(groupPacket);
                        continue;
                    }
                }
            }

            // Proximity check
            if (allowProximity && senderLoc != null) {
                CachedPlayerLocation otherLoc = positionCache.get(other.getPlayerUuid());
                if (otherLoc != null && otherLoc.worldName() != null && otherLoc.worldName().equals(senderLoc.worldName())) {
                    if (otherLoc.distanceSquared(senderLoc) <= maxDistSq) {
                        if (proximityPacket == null) {
                            proximityPacket = AudioPayloadHelper.createAudioBroadcastPacket(
                                session.getPlayerUuid(),
                                senderLoc.x(),
                                senderLoc.y(),
                                senderLoc.z(),
                                pcm
                            );
                        }
                        other.sendBinary(proximityPacket);
                    }
                }
            }
        }
    }

    /**
     * Handles control JSON messages from Web client (e.g. mute, deafen, ping).
     */
    private void handleTextMessage(WsMessageContext ctx) {
        WebVoiceSession session = sessionManager.getSessionByContext(ctx);
        if (session == null) return;

        try {
            JsonObject json = JsonParser.parseString(ctx.message()).getAsJsonObject();
            String type = json.has("type") ? json.get("type").getAsString() : "";

            switch (type) {
                case "mute" -> {
                    if (json.has("value")) {
                        boolean muted = json.get("value").getAsBoolean();
                        session.setMuted(muted);
                        sessionManager.updateVoicechatConnectionState(session.getPlayerUuid(), true, muted || session.isDeafened());
                    }
                }
                case "deafen" -> {
                    if (json.has("value")) {
                        boolean deafened = json.get("value").getAsBoolean();
                        session.setDeafened(deafened);
                        sessionManager.updateVoicechatConnectionState(session.getPlayerUuid(), true, session.isMuted() || deafened);
                    }
                }
                case "ping" -> {
                    session.sendText("{\"type\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}");
                }
                default -> {}
            }
        } catch (JsonSyntaxException | IllegalStateException ignored) {}
    }

    private void handleClose(WsCloseContext ctx) {
        sessionManager.unregisterSession(ctx);
    }

    private void handleError(WsErrorContext ctx) {
        Throwable err = ctx.error();
        if (err != null) {
            LOGGER.log(Level.FINE, "WebSocket error: {0}", err.getMessage());
        }
    }

    /**
     * Stops the embedded Javalin server.
     */
    public void stop() {
        if (app != null) {
            try {
                app.stop();
                LOGGER.info("Web Voice Server stopped.");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error stopping Web Voice Server: {0}", e.getMessage());
            }
        }
    }
}
