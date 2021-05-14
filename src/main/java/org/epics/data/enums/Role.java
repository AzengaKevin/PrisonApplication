package org.epics.data.enums;

public enum Role {
    Admin("admin"),
    Doctor("doctor"),
    TaskManager("task-manager"),
    Warden("warden"),
    Prisoner("prisoner");

    private String slug;

    Role(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }
}
