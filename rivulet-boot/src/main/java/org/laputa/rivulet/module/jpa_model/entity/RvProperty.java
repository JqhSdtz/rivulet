package org.laputa.rivulet.module.jpa_model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvBaseEntity;
import org.laputa.rivulet.module.jpa_model.enums.PropertyType;
import org.laputa.rivulet.module.jpa_model.enums.PropertyValueClassType;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
@Cache(region = "defaultCache", usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@DynamicInsert
@DynamicUpdate
@Title("数据模型属性")
@TableComment("数据模型属性与JPA实体类属性对应")
@Table(name = "rv_property")
public class RvProperty extends RvBaseEntity<String> {
    @Id
    @UuidGenerator
    @Title("属性ID")
    @Comment("属性ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @JsonBackReference("properties")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = CascadeType.PERSIST)
    @Title("对应模型ID")
    @Comment("属性所对应的数据模型的ID，使用外键关联")
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Title("属性名称")
    @Comment("属性名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("属性编码")
    @Comment("属性编码对应数据库中字段的名称，即字段的Name")
    @Column(name = "code", nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Title("属性类型")
    @Comment("属性对应的类型")
    @Column(name = "type", nullable = false)
    private PropertyType type;

    @Title("是否缓存")
    @Comment("该属性是否使用JPA的二级缓存机制")
    @Column(name = "use_cache")
    private Boolean useCache;

    @Title("关联实体类名")
    @Comment("用于在@ManyToOne，@OneToMany等关联关系中显示指定关联实体的类名，避免因泛型擦除导致jpa无法识别目标类")
    @Column(name = "target_entity_class_name")
    private String targetEntityClassName;

    @Title("关联关系级联类型")
    @Comment("用于在关联关系中指定级联类型，即CascadeType枚举类，包括ALL,PERSIST,MERGE,REMOVE,REFRESH,REFRESH。可指定多个，用英文逗号分隔开")
    @Column(name = "cascade_types")
    private String cascadeTypes;

    @Title("关联关系加载类型")
    @Comment("用于在关联关系中指定加载类型，即FetchType枚举类中的LAZY或EAGER")
    @Column(name = "fetch_type")
    private String fetchType;

    @Title("关联关系是否可选")
    @Comment("用于在关联关系中指定optional属性，即是否可选，true为可选，即不必选；false为必选")
    @Column(name = "optional")
    private Boolean optional;

    @Title("关联关系匹配字段")
    @Comment("用于在双向的关联关系中指定mappedBy属性")
    @Column(name = "mapped_by")
    private String mappedBy;

    @Title("关联关系是否孤立删除")
    @Comment("用于在关联关系中指定orphanRemoval属性，即当该实体从所有被关联的实体中移除，即变为孤立实体时，是否删除该实体")
    @Column(name = "orphan_removal")
    private Boolean orphanRemoval;

    @Enumerated(EnumType.STRING)
    @Title("数据类类型")
    @Comment("数据类对应的类型")
    @Column(name = "value_class_type", nullable = false)
    private PropertyValueClassType valueClassType;

    @Title("数据类类名")
    @Comment("数据类的类名，此处为全名")
    @Column(name = "value_class_name", nullable = false)
    private String valueClassName;

    @Title("默认值")
    @Comment("属性默认值，新增一条数据时，若该属性为空，则自动赋默认值")
    @Column(name = "default_value")
    private String defaultValue;

    @Title("备注")
    @Comment("用于对该属性的作用作解释说明")
    @Column(name = "remark")
    private String remark;

    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示属性，与数据库中实际的顺序无关联")
    @Column(name = "order_num")
    private Integer orderNum;

    @Title("可以为空")
    @Comment("可以为空标识用于标记该属性是否可以是空值")
    @DefaultValue(Strings.FALSE)
    @Column(name = "nullable")
    private Boolean nullable;
}
