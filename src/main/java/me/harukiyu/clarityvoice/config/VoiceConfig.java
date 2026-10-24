package me.harukiyu.clarityvoice.config;

import org.bukkit.configuration.file.FileConfiguration;

public class VoiceConfig {
    private final String host;
    private final int port;
    private final String publicUrl;
    private final double maxDistance;
    private final int otpDurationSeconds;
    private final long positionSyncIntervalTicks;
    private final String categoryName;

    public VoiceConfig(FileConfiguration config) {
        this.host = config.getString("server.host", "0.0.0.0");
        this.port = config.getInt("server.port", 8080);
        String rawUrl = config.getString("server.public_url", "http://localhost:8080");
        if (rawUrl != null) {
            rawUrl = rawUrl.trim();
            while (rawUrl.endsWith("/")) {
                rawUrl = rawUrl.substring(0, rawUrl.length() - 1);
            }
        }
        this.publicUrl = (rawUrl != null && !rawUrl.isBlank()) ? rawUrl : "http://localhost:8080";
        this.maxDistance = config.getDouble("voice.max_distance", 48.0);
        this.otpDurationSeconds = config.getInt("voice.otp_duration_seconds", 120);
        this.positionSyncIntervalTicks = config.getLong("voice.position_sync_interval_ticks", 2L);
        this.categoryName = config.getString("voice.category_name", "clarity_voice");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public int getOtpDurationSeconds() {
        return otpDurationSeconds;
    }

    public long getPositionSyncIntervalTicks() {
        return positionSyncIntervalTicks;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
