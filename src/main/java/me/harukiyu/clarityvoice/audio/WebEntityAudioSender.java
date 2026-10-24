package me.harukiyu.clarityvoice.audio;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles encoding PCM audio from a Web Client and transmitting it to Simple Voice Chat
 * players in-game via an EntityAudioChannel (proximity) and StaticAudioChannel (group).
 */
public class WebEntityAudioSender {

    private static final Logger LOGGER = Logger.getLogger(WebEntityAudioSender.class.getName());

    private final UUID playerUuid;
    private final EntityAudioChannel channel;
    private final OpusEncoder encoder;
    private final VoicechatServerApi voiceApi;
    private final String categoryName;

    private StaticAudioChannel groupChannel;
    private volatile boolean closed = false;

    public WebEntityAudioSender(UUID playerUuid, EntityAudioChannel channel, OpusEncoder encoder) {
        this(playerUuid, channel, encoder, null, "Clarity Voice");
    }

    public WebEntityAudioSender(UUID playerUuid, EntityAudioChannel channel, OpusEncoder encoder, VoicechatServerApi voiceApi, String categoryName) {
        this.playerUuid = playerUuid;
        this.channel = channel;
        this.encoder = encoder;
        this.voiceApi = voiceApi;
        this.categoryName = categoryName;
    }

    /**
     * Encodes 16-bit PCM samples into Opus and transmits to players in-game.
     *
     * @param pcmSamples array of 960 PCM samples
     */
    public synchronized void sendAudio(short[] pcmSamples) {
        sendAudio(pcmSamples, false, null);
    }

    /**
     * Encodes 16-bit PCM samples into Opus and transmits to players in-game,
     * including group members via StaticAudioChannel if in a voice group.
     */
    public synchronized void sendAudio(short[] pcmSamples, boolean inGroup, Group group) {
        if (closed || encoder == null) {
            return;
        }

        try {
            byte[] opusBytes = encoder.encode(pcmSamples);
            if (opusBytes == null || opusBytes.length == 0) {
                return;
            }

            // Proximity audio to nearby players (if not isolated group)
            boolean sendProximity = !inGroup || (group != null && group.getType() != Group.Type.ISOLATED);
            if (sendProximity && channel != null) {
                channel.send(opusBytes);
            }

            // Group audio to mod players in same group
            if (inGroup && group != null && voiceApi != null) {
                if (groupChannel == null) {
                    groupChannel = voiceApi.createStaticAudioChannel(UUID.randomUUID());
                    if (groupChannel != null && categoryName != null) {
                        groupChannel.setCategory(categoryName);
                    }
                }

                if (groupChannel != null) {
                    groupChannel.clearTargets();
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (online.getUniqueId().equals(playerUuid)) continue;
                        VoicechatConnection conn = voiceApi.getConnectionOf(online.getUniqueId());
                        if (conn != null && conn.isInstalled() && conn.isInGroup() && group.getId().equals(conn.getGroup().getId())) {
                            groupChannel.addTarget(conn);
                        }
                    }
                    groupChannel.send(opusBytes);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, e, () -> "Failed to encode/send audio for " + playerUuid);
        }
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public EntityAudioChannel getChannel() {
        return channel;
    }

    public boolean isClosed() {
        return closed;
    }

    public synchronized void close() {
        if (closed) return;
        closed = true;

        if (channel != null) {
            try {
                channel.flush();
            } catch (Exception ignored) {}
        }

        if (groupChannel != null) {
            try {
                groupChannel.flush();
            } catch (Exception ignored) {}
        }

        if (encoder != null && !encoder.isClosed()) {
            try {
                encoder.close();
            } catch (Exception ignored) {}
        }
    }
}
