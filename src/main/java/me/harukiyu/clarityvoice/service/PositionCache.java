package me.harukiyu.clarityvoice.service;

import me.harukiyu.clarityvoice.model.CachedPlayerLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache of player positions and orientations, updated each server tick
 * to enable lock-free, asynchronous spatial audio calculation.
 */
public class PositionCache {

    private final Map<UUID, CachedPlayerLocation> cache = new ConcurrentHashMap<>();

    /**
     * Updates cache from all online Bukkit players. Called on the server main thread.
     */
    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            cache.put(player.getUniqueId(), new CachedPlayerLocation(
                player.getUniqueId(),
                player.getName(),
                loc.getWorld() != null ? loc.getWorld().getName() : "world",
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
            ));
        }

        // Clean up disconnected players
        cache.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }

    public CachedPlayerLocation get(UUID uuid) {
        return cache.get(uuid);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public Collection<CachedPlayerLocation> getAll() {
        return cache.values();
    }
}
