package org.laputa.rivulet.module.data_model.entity.constraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
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
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.data_model.entity.RvIndex;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvUniqueColumn;
import org.laputa.rivulet.module.data_model.entity.inter.DataModelEntityInterface;

import java.util.List;

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
@Title("唯一性约束")
@TableComment("数据模型的唯一性约束对应数据库表的唯一性约束，用于约束数据模型中的一个属性或一组属性在全部数据中唯一，通常会有一个索引与之对应")
@Table(name = "rv_unique")
public class RvUnique extends RvEntity<String> implements DataModelEntityInterface {
    @Id
    @UuidGenerator
    @Title("唯一性约束ID")
    @Comment("唯一性约束ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("uniques")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = CascadeType.PERSIST)
    @Title("对应模型ID")
    @Comment("唯一性约束所对应的数据模型的ID，使用外键关联")
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该唯一性约束是否是系统内置")
    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @Title("唯一性约束名称")
    @Comment("唯一性约束名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("唯一性约束编码")
    @Comment("唯一性约束编码对应数据库中唯一性约束的名称，即唯一性约束的Name")
    @Column(name = "code", nullable = false)
    private String code;

    @Title("备注")
    @Comment("唯一性约束备注对应数据库中唯一性约束的注释，即唯一性约束的Comment")
    @Column(name = "remark")
    private String remark;

    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示唯一性约束，与数据库中实际的顺序无关联")
    @Column(name = "order_num")
    private Integer orderNum;

    @Title("对应索引")
    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "backing_index_id")
    private RvIndex backingIndex;

    @JsonManagedReference("uniqueColumns")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "unique")
    private List<RvUniqueColumn> uniqueColumns;

    @JsonSetter("uniqueColumns")
    public void setUniqueColumns(List<RvUniqueColumn> rvUniqueColumns) {
        this.uniqueColumns = rvUniqueColumns;
        if (rvUniqueColumns == null) {
            return;
        }
        rvUniqueColumns.forEach(rvUniqueColumn -> rvUniqueColumn.setUnique(this));
    }

}
