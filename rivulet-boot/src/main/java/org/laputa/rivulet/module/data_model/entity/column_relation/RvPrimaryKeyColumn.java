package org.laputa.rivulet.module.data_model.entity.column_relation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.data_model.entity.RvColumn;
import org.laputa.rivulet.module.data_model.entity.constraint.RvPrimaryKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import org.laputa.rivulet.module.data_model.entity.inter.DataModelEntityInterface;

/**
 * @author JQH
 * @since 下午 8:02 22/07/25
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@Title("主键与属性的关联模型")
@TableComment("主键与属性的关联模型用于记录某个数据模型中的主键与所涉及的属性之间的关联关系")
@Table(name = "rv_primary_key_column")
public class RvPrimaryKeyColumn extends RvEntity<String> {
    @Id
    @UuidGenerator
    @Title("关联ID")
    @Comment("关联ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("primaryKeyColumns")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @Title("对应主键ID")
    @Comment("该关联记录所对应的主键的ID，使用外键关联")
    @JoinColumn(name = "primary_key_id")
    private RvPrimaryKey primaryKey;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该唯一性约束是否是系统内置")
    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Title("对应属性ID")
    @Comment("该关联记录所对应的属性的ID，使用外键关联")
    @JoinColumn(name = "column_id")
    private RvColumn column;

    @Title("备注")
    @Comment("关联关系备注对应数据库中关联关系的注释")
    @Column(name = "remark")
    private String remark;

    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示该关联关系对应的属性，与数据库中实际的顺序无关联")
    @Column(name = "order_num")
    private Integer orderNum;
}
