package org.laputa.rivulet.module.jpa_model.enums;

public enum PropertyType {
    /**
     * 基础类型，即@column表示的字段
     */
    Attribute,
    /**
     * 关联字段类型，即@JoinColumn加@ManyToOne表示的字段
     */
    ManyToOne,
    /**
     * 关联字段类型，即@JoinColumn加@OneToOne表示的字段
     */
    OneToOne,
    /**
     * 关联字段类型，即@JoinColumn加@OneToMany表示的字段
     */
    OneToMany,
    /**
     * 关联字段类型，即@JoinColumn加@ManyToMany表示的字段
     */
    ManyToMany
}
