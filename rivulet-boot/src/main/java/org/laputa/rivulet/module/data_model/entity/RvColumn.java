package org.laputa.rivulet.module.data_model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.persistence.*;
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
import org.laputa.rivulet.module.data_model.entity.inter.DataModelEntityInterface;

import java.math.BigInteger;

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
@Title("数据模型属性")
@TableComment("数据模型属性与数据库中表的字段对应，用于描述一个数据模型的具体构成，是数据存储的基本单位")
@Table(name = "rv_column")
public class RvColumn extends RvEntity<String> implements DataModelEntityInterface {
    @Id
    @UuidGenerator
    @Title("属性ID")
    @Comment("属性ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("columns")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = CascadeType.PERSIST)
    @Title("对应模型ID")
    @Comment("属性所对应的数据模型的ID，使用外键关联")
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该索引是否是系统内置")
    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @Title("属性名称")
    @Comment("属性名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("属性编码")
    @Comment("属性编码对应数据库中字段的名称，即字段的Name")
    @Column(name = "code", nullable = false)
    private String code;

    @Title("数据类型")
    @Comment("""
            数据类型对应数据库中字段的数据类型，考虑到数据库无关性，这里的数据类型使用Liquibase中定义的数据类型，
            并通过Liquibase转换为实际落地的数据库的字段类型，具体转换关系见Liquibase文档：
            https://docs.liquibase.com/concepts/data-type-handling.html""")
    @Column(name = "data_type", nullable = false)
    private String dataType;


    @Column(name = "auto_increment")
    @Title("自增")
    @Comment("自增标识用于标记当前属性在数据库中对应的字段是否为自增字段，即每插入一行数据，该字段自动在上一行的基础上+1，一般用于主键")
    @DefaultValue(Strings.FALSE)
    private Boolean autoIncrement;

    @Column(name = "start_with")
    @Title("自增起点")
    @Comment("自增起点，仅自增标识为真时起作用")
    @DefaultValue("0")
    private BigInteger startWith;

    @Column(name = "increment_by")
    @Title("自增步长")
    @Comment("自增步长，仅自增标识为真时起作用")
    @DefaultValue("1")
    private BigInteger incrementBy;

    @Column(name = "default_value")
    @Title("默认值")
    @Comment("属性默认值，新增一条数据时，若该属性为空，则自动赋默认值")
    private String defaultValue;

    @Column(name = "remark")
    @Title("备注")
    @Comment("用于对该属性的作用作解释说明")
    private String remark;

    @Column(name = "order_num")
    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示属性，与数据库中实际的顺序无关联")
    private Integer orderNum;

    @Column(name = "nullable")
    @Title("可以为空")
    @Comment("可以为空标识用于标记该属性是否可以是空值")
    @DefaultValue(Strings.FALSE)
    private Boolean nullable;

    @Column(name = "descending")
    @Title("降序")
    @Comment("是否降序，MySQL8中有降序索引，如果为true，则在降序索引中该列标记为降序")
    @DefaultValue(Strings.FALSE)
    private Boolean descending;
}
