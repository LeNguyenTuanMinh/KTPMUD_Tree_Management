package com.beepollen.entity;

public enum CollectionSource {
    MANUAL("Thủ công"),
    IOT_SIMULATED("IoT");

    private final String displayName;

    CollectionSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
