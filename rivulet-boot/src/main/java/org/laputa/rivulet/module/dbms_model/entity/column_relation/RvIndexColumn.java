package org.laputa.rivulet.module.dbms_model.entity.column_relation;

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
import org.hibernate.annotations.Cache;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvBaseEntity;
import org.laputa.rivulet.module.dbms_model.entity.RvColumn;
import org.laputa.rivulet.module.dbms_model.entity.RvIndex;

/**
 * @author JQH
 * @since 下午 8:02 22/07/25
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
@Cache(region = "defaultCache", usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicInsert
@DynamicUpdate
@Title("索引与属性的关联模型")
@TableComment("索引与属性的关联模型用于记录某个数据模型中的索引与所涉及的属性之间的关联关系")
@Table(name = "rv_index_column")
public class RvIndexColumn extends RvBaseEntity<String> {
    @Id
    @UuidGenerator
    @Title("关联ID")
    @Comment("关联ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @Title("对应索引ID")
    @Comment("该关联记录所对应的索引的ID，使用外键关联")
    @JsonBackReference("indexColumns")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "index_id")
    private RvIndex index;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该唯一性约束是否是系统内置")
    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @Title("对应属性ID")
    @Comment("该关联记录所对应的属性的ID，使用外键关联")
    @ManyToOne(cascade = CascadeType.PERSIST)
    @OnDelete(action = OnDeleteAction.CASCADE)
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
