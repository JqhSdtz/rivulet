package org.laputa.rivulet.module.jpa_model.enums;

public enum PropertyValueClassType {
    /**
     * 基础类如String
     */
    Basic("Basic"),
    /**
     * 集合类如List
     */
    Collector("Collector"),
    /**
     * 业务类如RvPrototype
     */
    Business("Business");

    private final String value;

    PropertyValueClassType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
