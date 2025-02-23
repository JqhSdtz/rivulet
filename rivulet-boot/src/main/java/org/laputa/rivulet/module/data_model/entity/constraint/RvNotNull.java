package org.laputa.rivulet.module.data_model.entity.constraint;

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
import org.laputa.rivulet.module.data_model.entity.RvPrototype;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.laputa.rivulet.module.data_model.entity.inter.DataModelEntityInterface;

/**
 * @author JQH
 * @since 下午 7:06 23/04/15
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@Title("非空约束")
@TableComment("数据模型的非空约束对应数据库表的非空约束，用于约束数据模型中的一个属性不为空，通常会有一个索引与之对应")
@Table(name = "rv_not_null")
public class RvNotNull extends RvEntity<String> implements DataModelEntityInterface {
    @Id
    @UuidGenerator
    @Title("非空约束ID")
    @Comment("非空约束ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @JsonBackReference("notNulls")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @Title("对应模型ID")
    @Comment("非空约束所对应的数据模型的ID，使用外键关联")
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该非空约束是否是系统内置")
    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @Title("非空约束名称")
    @Comment("非空约束名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("非空约束编码")
    @Comment("非空约束编码对应数据库中非空约束的名称，即非空约束的Name")
    @Column(name = "code", nullable = false)
    private String code;

    @Title("备注")
    @Comment("非空约束备注对应数据库中非空约束的注释，即非空约束的Comment")
    @Column(name = "remark")
    private String remark;

    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示非空约束，与数据库中实际的顺序无关联")
    @Column(name = "order_num")
    private Integer orderNum;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "column_id")
    @Title("对应属性ID")
    @Comment("非空约束所对应的数据模型的属性的ID，使用外键关联")
    private RvColumn column;

}
