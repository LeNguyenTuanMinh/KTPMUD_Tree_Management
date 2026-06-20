package com.beepollen.entity;

/**
 * Enum representing the health status of a bee colony.
 */
public enum HealthStatus {
    HEALTHY("Khỏe mạnh"),
    WEAK("Yếu"),
    CRITICAL("Nguy kịch"),
    DEAD("Đã chết");

    private final String displayName;

    HealthStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
