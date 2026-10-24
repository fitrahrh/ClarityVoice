package me.harukiyu.clarityvoice.service;

import me.harukiyu.clarityvoice.model.OtpEntry;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages generation, storage, and validation of 6-digit OTP codes.
 */
public class OtpManager {

    private final Map<String, OtpEntry> otpToEntry = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToOtp = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final int durationSeconds;

    public OtpManager(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    /**
     * Generates a unique 6-digit OTP code for a player, invalidating any previous code.
     *
     * @param playerUuid UUID of the player
     * @return 6-digit OTP code string
     */
    public synchronized String generateOtp(UUID playerUuid) {
        // Invalidate existing OTP if present
        String existingCode = playerToOtp.remove(playerUuid);
        if (existingCode != null) {
            otpToEntry.remove(existingCode);
        }

        cleanExpired();

        String code;
        int attempts = 0;
        do {
            int num = 100000 + random.nextInt(900000);
            code = String.valueOf(num);
            attempts++;
            if (attempts > 50) {
                // Fallback in case of collision
                code = String.format("%06d", (System.currentTimeMillis() + random.nextInt(10000)) % 1000000);
            }
        } while (otpToEntry.containsKey(code));

        long expiresAt = System.currentTimeMillis() + (durationSeconds * 1000L);
        OtpEntry entry = new OtpEntry(playerUuid, code, expiresAt);

        otpToEntry.put(code, entry);
        playerToOtp.put(playerUuid, code);

        return code;
    }

    /**
     * Validates and consumes the given OTP code.
     *
     * @param code 6-digit OTP code
     * @return UUID of player if valid, or null if invalid or expired
     */
    public synchronized UUID validateAndConsume(String code) {
        if (code == null) return null;
        code = code.trim();

        OtpEntry entry = otpToEntry.remove(code);
        if (entry == null) {
            return null;
        }

        playerToOtp.remove(entry.playerUuid());

        if (entry.isExpired()) {
            return null;
        }

        return entry.playerUuid();
    }

    /**
     * Invalidates any active OTP code for the given player.
     */
    public synchronized void invalidate(UUID playerUuid) {
        String code = playerToOtp.remove(playerUuid);
        if (code != null) {
            otpToEntry.remove(code);
        }
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        otpToEntry.entrySet().removeIf(e -> e.getValue().expiresAtMillis() < now);
    }
}
