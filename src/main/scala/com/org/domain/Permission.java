package com.org.domain;

import java.util.Objects;

import static java.util.Arrays.stream;

public enum Permission {
    EDIT_DATASETS, SUPER, CREATE_POSTS, ANON;

    public static Permission fromString(String value) {
        return stream(values())
                .filter(enumValue -> Objects.equals(enumValue.name(), value))
                .findFirst()
                .orElse(ANON);
    }
}
