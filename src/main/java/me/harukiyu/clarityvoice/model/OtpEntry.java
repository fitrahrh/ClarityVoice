package me.harukiyu.clarityvoice.model;

import java.util.UUID;

public record OtpEntry(
    UUID playerUuid,
    String code,
    long expiresAtMillis
) {
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAtMillis;
    }
}
