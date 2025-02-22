package org.laputa.rivulet.module.data_model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvEntity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.laputa.rivulet.module.data_model.entity.inter.WithBuiltInFlag;

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
@TableComment("数据模型字段")
@Table(name = "rv_column")
public class RvColumn extends RvEntity<String> implements WithBuiltInFlag {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("columns")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "auto_increment")
    @Comment("自增标识")
    @DefaultValue(Strings.FALSE)
    private Boolean autoIncrement;

    @Column(name = "start_with")
    @Comment("自增起点")
    @DefaultValue("0")
    private BigInteger startWith;

    @Column(name = "increment_by")
    @Comment("自增步长")
    @DefaultValue("1")
    private BigInteger incrementBy;

    @Column(name = "default_value")
    @Comment("默认值")
    private String defaultValue;

    @Column(name = "remark")
    @Comment("字段注释")
    private String remark;

    @Column(name = "order_num")
    @Comment("排序号")
    private Integer orderNum;

    @Column(name = "nullable")
    @Comment("是否可以为空值")
    @DefaultValue(Strings.FALSE)
    private Boolean nullable;

    @Column(name = "descending")
    @Comment("是否降序，MySQL8中有降序索引，如果为true，则在降序索引中该列标记为降序")
    @DefaultValue(Strings.FALSE)
    private Boolean descending;
}
