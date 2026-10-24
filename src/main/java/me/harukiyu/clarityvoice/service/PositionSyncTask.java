package me.harukiyu.clarityvoice.service;

import com.google.gson.Gson;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import me.harukiyu.clarityvoice.config.VoiceConfig;
import me.harukiyu.clarityvoice.model.CachedPlayerLocation;
import me.harukiyu.clarityvoice.voice.ClarityVoiceChatPlugin;
import me.harukiyu.clarityvoice.web.WebVoiceSession;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Periodically updates the position cache and synchronizes 3D spatial positions,
 * orientations, and voice group status to all connected Web Voice clients.
 */
public class PositionSyncTask extends BukkitRunnable {

    private final PositionCache positionCache;
    private final SessionManager sessionManager;
    private final VoiceConfig config;
    private final ClarityVoiceChatPlugin voicePlugin;
    private final Gson gson = new Gson();

    public PositionSyncTask(PositionCache positionCache, SessionManager sessionManager, VoiceConfig config, ClarityVoiceChatPlugin voicePlugin) {
        this.positionCache = positionCache;
        this.sessionManager = sessionManager;
        this.config = config;
        this.voicePlugin = voicePlugin;
    }

    public PositionSyncTask(PositionCache positionCache, SessionManager sessionManager, VoiceConfig config) {
        this(positionCache, sessionManager, config, null);
    }

    @Override
    public void run() {
        // 1. Update the position cache with latest Bukkit positions
        positionCache.updateAll();

        Collection<WebVoiceSession> activeSessions = sessionManager.getAllSessions();
        if (activeSessions.isEmpty()) {
            return;
        }

        double maxDist = config.getMaxDistance();
        double searchRadiusSq = (maxDist * 1.5) * (maxDist * 1.5);
        Collection<CachedPlayerLocation> allPlayers = positionCache.getAll();
        VoicechatServerApi voiceApi = voicePlugin != null ? voicePlugin.getServerApi() : null;

        // 2. Dispatch customized 3D position state to each active web client
        for (WebVoiceSession session : activeSessions) {
            if (!session.isOpen()) continue;

            CachedPlayerLocation self = positionCache.get(session.getPlayerUuid());
            if (self == null) continue;

            VoicechatConnection selfConn = voiceApi != null ? voiceApi.getConnectionOf(session.getPlayerUuid()) : null;
            Group selfGroup = (selfConn != null && selfConn.isInGroup()) ? selfConn.getGroup() : null;
            boolean selfInGroup = selfGroup != null;

            List<Map<String, Object>> nearbyList = new ArrayList<>();
            for (CachedPlayerLocation other : allPlayers) {
                if (other.uuid().equals(self.uuid())) continue;
                if (!other.worldName().equals(self.worldName())) continue;

                double distSq = self.distanceSquared(other);
                if (distSq <= searchRadiusSq) {
                    Map<String, Object> nearbyData = new LinkedHashMap<>();
                    nearbyData.put("uuid", other.uuid().toString());
                    nearbyData.put("name", other.name());
                    nearbyData.put("x", Math.round(other.x() * 100.0) / 100.0);
                    nearbyData.put("y", Math.round(other.y() * 100.0) / 100.0);
                    nearbyData.put("z", Math.round(other.z() * 100.0) / 100.0);
                    nearbyData.put("distance", Math.round(Math.sqrt(distSq) * 10.0) / 10.0);

                    // Check if other player is in the same voice group
                    boolean inSameGroup = false;
                    if (selfInGroup && selfGroup != null && voiceApi != null) {
                        VoicechatConnection otherConn = voiceApi.getConnectionOf(other.uuid());
                        if (otherConn != null && otherConn.isInGroup()) {
                            Group otherGroup = otherConn.getGroup();
                            if (otherGroup != null && selfGroup.getId() != null && selfGroup.getId().equals(otherGroup.getId())) {
                                inSameGroup = true;
                            }
                        }
                    }
                    nearbyData.put("inGroup", inSameGroup);

                    nearbyList.add(nearbyData);
                }
            }

            // Sort by nearest first
            nearbyList.sort(Comparator.comparingDouble(a -> (double) a.get("distance")));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "position_sync");

            Map<String, Object> selfData = new LinkedHashMap<>();
            selfData.put("x", self.x());
            selfData.put("y", self.y());
            selfData.put("z", self.z());
            selfData.put("yaw", self.yaw());
            selfData.put("pitch", self.pitch());
            selfData.put("world", self.worldName());
            payload.put("self", selfData);

            // Voice Group status
            if (selfInGroup && selfGroup != null) {
                Map<String, Object> groupData = new LinkedHashMap<>();
                groupData.put("id", selfGroup.getId().toString());
                groupData.put("name", selfGroup.getName());
                String typeStr = "NORMAL";
                if (selfGroup.getType() == Group.Type.OPEN) {
                    typeStr = "OPEN";
                } else if (selfGroup.getType() == Group.Type.ISOLATED) {
                    typeStr = "ISOLATED";
                }
                groupData.put("type", typeStr);
                payload.put("group", groupData);
            } else {
                payload.put("group", null);
            }

            payload.put("nearby", nearbyList);
            payload.put("maxDistance", maxDist);

            session.sendText(gson.toJson(payload));
        }
    }
}
