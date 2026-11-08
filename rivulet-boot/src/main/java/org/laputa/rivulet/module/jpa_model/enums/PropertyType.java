package org.laputa.rivulet.module.jpa_model.enums;

public enum PropertyType {
    /**
     * Id类型，即添加@Id注解的字段，统一使用UUID
     */
    Id("Id"),
    /**
     * 基础类型，即@column表示的字段
     */
    Attribute("Attribute"),
    /**
     * 关联字段类型，即@JoinColumn加@ManyToOne表示的字段
     */
    ManyToOne("ManyToOne"),
    /**
     * 关联字段类型，即@JoinColumn加@OneToOne表示的字段
     */
    OneToOne("OneToOne"),
    /**
     * 关联字段类型，即@JoinColumn加@OneToMany表示的字段
     */
    OneToMany("OneToMany"),
    /**
     * 关联字段类型，即@JoinColumn加@ManyToMany表示的字段
     */
    ManyToMany("ManyToMany");

    private final String value;

    PropertyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
