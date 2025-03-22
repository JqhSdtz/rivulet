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
import org.laputa.rivulet.module.data_model.entity.column_relation.RvForeignKeyForeignColumn;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvForeignKeyTargetColumn;
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
@Title("外键")
@TableComment("""
        数据模型的外键对应数据库表的外键，用于表示一个数据模型中的某个属性与另一个数据模型中的某个属性存在关联关系，
        比如班级表中的级部ID对应级部表中的ID。外键关联的数据之间应该为包含关系，比如班级是包含在级部中的，级部没有了，班级自然也没有了。
        非包含关系的两个数据之间若要关联，则需要添加一个中间表，比如班级和学生之间需要有一个学生分班表，因为即使班级没有了，学生依然存在。
        但是班级或学生任意一个不存在了，则分班表中相关的数据也都会随之删除。""")
@Table(name = "rv_foreign_key")
public class RvForeignKey extends RvEntity<String> implements DataModelEntityInterface {
    @Id
    @UuidGenerator
    @Title("外键ID")
    @Comment("外键ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @Title("对应模型ID")
    @Comment("外键所对应的数据模型的ID，使用外键关联")
    @JsonBackReference("foreignKeys")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该外键是否是系统内置")
    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @Title("外键名称")
    @Comment("外键名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("外键编码")
    @Comment("外键编码对应数据库中外键的名称，即外键的Name")
    @Column(name = "code", nullable = false)
    private String code;

    @Title("级联删除")
    @Comment("级联删除标记若为真，则该外键对应的数据模型中的数据删除时，通过该外键连接的当前数据模型中的对应数据也会被删除，一般设为真")
    @Column(name = "cascade_delete")
    private Boolean cascadeDelete;

    @Title("目标模型ID")
    @Comment("外键目标所对应的数据模型的ID")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "target_prototype_id")
    private RvPrototype targetPrototype;

    @Title("备注")
    @Comment("外键备注对应数据库中外键的注释，即外键的Comment")
    @Column(name = "remark")
    private String remark;

    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示外键，与数据库中实际的顺序无关联")
    @Column(name = "order_num")
    private Integer orderNum;

    @Title("对应索引")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "backing_index_id")
    private RvIndex backingIndex;

    @JsonManagedReference("foreignKeyTargetColumns")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "foreignKey")
    private List<RvForeignKeyTargetColumn> foreignKeyTargetColumns;

    @JsonSetter("foreignKeyTargetColumns")
    public void setForeignKeyTargetColumns(List<RvForeignKeyTargetColumn> foreignKeyTargetColumns) {
        this.foreignKeyTargetColumns = foreignKeyTargetColumns;
        if (foreignKeyTargetColumns == null) {
            return;
        }
        foreignKeyTargetColumns.forEach(foreignKeyTargetColumn -> foreignKeyTargetColumn.setForeignKey(this));
    }

    @JsonManagedReference("foreignKeyForeignColumns")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "foreignKey")
    private List<RvForeignKeyForeignColumn> foreignKeyForeignColumns;

    @JsonSetter("foreignKeyForeignColumns")
    public void setForeignKeyForeignColumns(List<RvForeignKeyForeignColumn> foreignKeyForeignColumns) {
        this.foreignKeyForeignColumns = foreignKeyForeignColumns;
        if (foreignKeyForeignColumns == null) {
            return;
        }
        foreignKeyForeignColumns.forEach(foreignKeyForeignColumn -> foreignKeyForeignColumn.setForeignKey(this));
    }

}
