package me.harukiyu.clarityvoice.voice;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import me.harukiyu.clarityvoice.audio.AudioPayloadHelper;
import me.harukiyu.clarityvoice.config.VoiceConfig;
import me.harukiyu.clarityvoice.model.CachedPlayerLocation;
import me.harukiyu.clarityvoice.service.PositionCache;
import me.harukiyu.clarityvoice.service.SessionManager;
import me.harukiyu.clarityvoice.web.WebVoiceSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Voice Chat API integration.
 * Intercepts mod voice packets (Mod -> Web) and provides API access.
 */
public class ClarityVoiceChatPlugin implements VoicechatPlugin {

    public static final String PLUGIN_ID = "clarity_voice";
    private static final Logger LOGGER = Logger.getLogger(ClarityVoiceChatPlugin.class.getName());

    private final VoiceConfig config;
    private final PositionCache positionCache;
    private final SessionManager sessionManager;

    private volatile VoicechatServerApi serverApi;

    public ClarityVoiceChatPlugin(VoiceConfig config, PositionCache positionCache, SessionManager sessionManager) {
        this.config = config;
        this.positionCache = positionCache;
        this.sessionManager = sessionManager;
    }

    public ClarityVoiceChatPlugin(VoiceConfig config, PositionCache positionCache, SessionManager sessionManager, Logger logger) {
        this(config, positionCache, sessionManager);
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        LOGGER.info("ClarityVoice VoicechatPlugin initialized.");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(VoicechatServerStoppedEvent.class, ignored -> onServerStopped());
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        this.serverApi = event.getVoicechat();
        this.sessionManager.setVoiceApi(this.serverApi);
        LOGGER.info("Simple Voice Chat Server API attached to ClarityVoice.");

        try {
            VolumeCategory category = serverApi.volumeCategoryBuilder()
                .setId(config.getCategoryName())
                .setName("Clarity Voice")
                .setDescription("Audio proximity dari pemain yang terhubung via Web")
                .build();
            serverApi.registerVolumeCategory(category);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not register volume category: {0}", e.getMessage());
        }
    }

    private void onServerStopped() {
        this.sessionManager.setVoiceApi(null);
        this.serverApi = null;
        LOGGER.info("Simple Voice Chat Server API detached.");
    }

    /**
     * Intercepts audio packets sent by in-game players with the Simple Voice Chat mod,
     * decodes Opus to PCM, and relays them:
     * - Group members: via non-positional Group Audio Packet (0x03)
     * - Proximity listeners: via 3D Positional Audio Packet (0x01)
     */
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        if (serverApi == null) return;

        VoicechatConnection senderConn = event.getSenderConnection();
        if (senderConn == null || senderConn.getPlayer() == null) return;

        UUID senderUuid = senderConn.getPlayer().getUuid();
        de.maxhenkel.voicechat.api.Group senderGroup = senderConn.isInGroup() ? senderConn.getGroup() : null;
        boolean inGroup = senderGroup != null;

        // 1. Get sender location (prefer thread-safe cache)
        CachedPlayerLocation senderLoc = positionCache.get(senderUuid);
        if (senderLoc == null) {
            Player p = Bukkit.getPlayer(senderUuid);
            if (p == null) return;
            Location loc = p.getLocation();
            World w = loc.getWorld();
            senderLoc = new CachedPlayerLocation(
                senderUuid,
                p.getName(),
                w != null ? w.getName() : "world",
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
            );
        }

        // 2. Classify Web recipients into Group and Proximity
        double maxDist = config.getMaxDistance();
        double maxDistSq = maxDist * maxDist;
        List<WebVoiceSession> groupRecipients = new ArrayList<>();
        List<WebVoiceSession> proximityRecipients = new ArrayList<>();

        boolean allowProximity = !inGroup || (senderGroup != null && senderGroup.getType() != de.maxhenkel.voicechat.api.Group.Type.ISOLATED);

        for (WebVoiceSession webSession : sessionManager.getAllSessions()) {
            if (webSession.getPlayerUuid().equals(senderUuid)) continue;
            if (webSession.isDeafened()) continue;

            // Check group membership
            if (inGroup && senderGroup != null) {
                VoicechatConnection webConn = serverApi.getConnectionOf(webSession.getPlayerUuid());
                if (webConn != null && webConn.isInGroup()) {
                    de.maxhenkel.voicechat.api.Group webGroup = webConn.getGroup();
                    if (webGroup != null && senderGroup.getId() != null && senderGroup.getId().equals(webGroup.getId())) {
                        groupRecipients.add(webSession);
                        continue; // Skip proximity if already in group
                    }
                }
            }

            // Check proximity
            if (allowProximity) {
                CachedPlayerLocation webLoc = positionCache.get(webSession.getPlayerUuid());
                if (webLoc != null && senderLoc.worldName() != null && senderLoc.worldName().equals(webLoc.worldName())) {
                    if (webLoc.distanceSquared(senderLoc) <= maxDistSq) {
                        proximityRecipients.add(webSession);
                    }
                }
            }
        }

        if (groupRecipients.isEmpty() && proximityRecipients.isEmpty()) return;

        // 3. Decode Opus packet into 16-bit PCM
        byte[] opusData = event.getPacket().getOpusEncodedData();
        if (opusData == null || opusData.length == 0) return;

        OpusDecoder decoder = sessionManager.getOrCreateDecoder(senderUuid, serverApi);
        if (decoder == null) return;

        short[] pcm;
        synchronized (decoder) {
            pcm = decoder.decode(opusData);
        }

        if (pcm == null || pcm.length == 0) return;

        // 4. Relay Group audio packets (0x03)
        if (!groupRecipients.isEmpty() && senderGroup != null) {
            byte[] groupPacket = AudioPayloadHelper.createGroupAudioPacket(
                senderUuid,
                senderGroup.getId(),
                pcm
            );
            for (WebVoiceSession recipient : groupRecipients) {
                recipient.sendBinary(groupPacket);
            }
        }

        // 5. Relay Proximity 3D audio packets (0x01)
        if (!proximityRecipients.isEmpty()) {
            byte[] proximityPacket = AudioPayloadHelper.createAudioBroadcastPacket(
                senderUuid,
                senderLoc.x(),
                senderLoc.y(),
                senderLoc.z(),
                pcm
            );
            for (WebVoiceSession recipient : proximityRecipients) {
                recipient.sendBinary(proximityPacket);
            }
        }
    }

    public VoicechatServerApi getServerApi() {
        return serverApi;
    }
}
