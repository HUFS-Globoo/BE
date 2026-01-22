// src/main/java/com/Globoo/user/domain/Campus.java
package com.Globoo.user.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Campus {
    SEOUL, GLOBAL;

    @JsonCreator
    public static Campus from(String v) {
        if (v == null) return null;
        return Campus.valueOf(v.trim().toUpperCase());
    }
}
