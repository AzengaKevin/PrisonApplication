package org.epics.data.enums;

import java.util.Arrays;

public enum Role {
    Admin("admin"),
    Doctor("doctor"),
    Guard("guard");

    private final String slug;

    Role(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public static Role fromFromSlug(String slug) {

        return Arrays.stream(Role.values()).filter(role -> role.slug.equals(slug))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
