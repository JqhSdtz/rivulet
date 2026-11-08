package org.laputa.rivulet.module.app.enums;

public enum AppLogSource {
    SERVER_JS("SERVER_JS"),
    SERVER_JAVA("SERVER_JAVA");

    private final String value;

    AppLogSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
