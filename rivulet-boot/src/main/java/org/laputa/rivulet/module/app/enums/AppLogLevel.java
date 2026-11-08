package org.laputa.rivulet.module.app.enums;

public enum AppLogLevel {
    TRACE("TRACE"),
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");

    private final String value;

    AppLogLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
