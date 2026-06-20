package com.beepollen.entity;

/**
 * Enum representing the different roles a user can have in the system.
 */
public enum Role {
    ADMIN("Quản trị viên"),
    RESEARCHER("Nghiên cứu viên"),
    BEEKEEPER("Người nuôi ong"),
    STUDENT("Học viên"),
    FARMER("Nông dân");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
