package org.epics.data.enums;

import java.util.Arrays;

public enum Gender {

    Male("male"), Female("female"), Other("other");

    String slug;

    Gender(String slug) {
        this.slug = slug;
    }

    @Override
    public String toString() {
        return slug;
    }

    public static Gender fromString(String slug) {
        return Arrays.stream(Gender.values()).filter(gender -> gender.slug.equals(slug))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
