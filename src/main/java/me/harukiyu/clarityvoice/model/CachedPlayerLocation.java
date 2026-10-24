package me.harukiyu.clarityvoice.model;

import java.util.UUID;

public record CachedPlayerLocation(
    UUID uuid,
    String name,
    String worldName,
    double x,
    double y,
    double z,
    float yaw,
    float pitch
) {
    public double distanceSquared(CachedPlayerLocation other) {
        if (other == null || !this.worldName.equals(other.worldName)) {
            return Double.MAX_VALUE;
        }
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public double distance(CachedPlayerLocation other) {
        return Math.sqrt(distanceSquared(other));
    }
}
