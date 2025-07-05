package org.laputa.rivulet.module.dbms_model.entity.constraint;

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
import org.laputa.rivulet.common.entity.RvBaseEntity;
import org.laputa.rivulet.module.dbms_model.entity.RvIndex;
import org.laputa.rivulet.module.dbms_model.entity.RvTable;
import org.laputa.rivulet.module.dbms_model.entity.column_relation.RvPrimaryKeyColumn;
import org.laputa.rivulet.module.dbms_model.entity.inter.DataModelEntityInterface;

import java.util.ArrayList;
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
@Title("主键")
@TableComment("数据模型的主键对应数据库表的主键，用于唯一标识一条数据记录，主键不可更改且一定唯一")
@Table(name = "rv_primary_key")
public class RvPrimaryKey extends RvBaseEntity<String> implements DataModelEntityInterface {
    @Id
    @UuidGenerator
    @Title("主键ID")
    @Comment("主键ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("primaryKey")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    @Title("对应模型ID")
    @Comment("主键所对应的数据模型的ID，使用外键关联")
    @JoinColumn(name = "table_id")
    private RvTable table;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该主键是否是系统内置")
    @DefaultValue(Strings.FALSE)
    @Column(name = "built_in", nullable = false)
    private Boolean builtIn;

    @Title("主键名称")
    @Comment("主键名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("主键编码")
    @Comment("主键编码对应数据库中主键的名称，即主键的Name")
    @Column(name = "code", nullable = false)
    private String code;

    @Title("备注")
    @Comment("主键备注对应数据库中主键的注释，即主键的Comment")
    @Column(name = "remark")
    private String remark;

    @JsonManagedReference("primaryKeyColumns")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "primaryKey")
    private List<RvPrimaryKeyColumn> primaryKeyColumns;

    @Title("对应索引")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "backing_index_id")
    private RvIndex backingIndex;

    @JsonSetter("primaryKeyColumns")
    public void setPrimaryKeyColumns(List<RvPrimaryKeyColumn> primaryKeyColumns) {
        if (primaryKeyColumns == null) {
            return;
        }
        if (this.primaryKeyColumns == null) {
            this.primaryKeyColumns = new ArrayList<>();
        } else {
            this.primaryKeyColumns.clear();
            this.primaryKeyColumns.addAll(primaryKeyColumns);
        }
        primaryKeyColumns.forEach(primaryKeyColumn -> primaryKeyColumn.setPrimaryKey(this));
    }
}
