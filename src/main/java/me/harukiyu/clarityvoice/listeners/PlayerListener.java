package me.harukiyu.clarityvoice.listeners;

import me.harukiyu.clarityvoice.service.OtpManager;
import me.harukiyu.clarityvoice.service.PositionCache;
import me.harukiyu.clarityvoice.service.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for player quit events to automatically clean up active Web Voice sessions,
 * audio channels, codecs, and cached locations.
 */
public class PlayerListener implements Listener {

    private final OtpManager otpManager;
    private final SessionManager sessionManager;
    private final PositionCache positionCache;

    public PlayerListener(OtpManager otpManager, SessionManager sessionManager, PositionCache positionCache) {
        this.otpManager = otpManager;
        this.sessionManager = sessionManager;
        this.positionCache = positionCache;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 1. Invalidate any pending OTP
        if (otpManager != null) {
            otpManager.invalidate(player.getUniqueId());
        }

        // 2. Disconnect active WebSocket session and destroy voice channels/decoders
        if (sessionManager != null) {
            sessionManager.cleanupPlayer(player.getUniqueId());
        }

        // 3. Remove cached position
        if (positionCache != null) {
            positionCache.remove(player.getUniqueId());
        }
    }
}
