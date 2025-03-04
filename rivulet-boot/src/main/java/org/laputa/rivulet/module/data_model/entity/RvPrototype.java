package org.laputa.rivulet.module.data_model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.laputa.rivulet.module.data_model.entity.constraint.RvForeignKey;
import org.laputa.rivulet.module.data_model.entity.constraint.RvNotNull;
import org.laputa.rivulet.module.data_model.entity.constraint.RvPrimaryKey;
import org.laputa.rivulet.module.data_model.entity.constraint.RvUnique;
import org.laputa.rivulet.module.data_model.entity.inter.DataModelEntityInterface;

import java.util.Date;
import java.util.List;

/**
 * @author JQH
 * @since 下午 8:17 22/01/30
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@DynamicInsert
@DynamicUpdate
@Title("数据模型")
@TableComment("数据模型和数据库表对应，包含属性、索引、外键等，用于描述一个结构化的数据")
@Table(name = "rv_prototype")
public class RvPrototype extends RvEntity<String> implements DataModelEntityInterface {
    @Id
    @UuidGenerator
    @Title("模型ID")
    @Comment("模型ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Title("模型名称")
    @Comment("模型名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("模型编码")
    @Comment("模型编码对应数据库中表的名称，即表的Name，但对于应用来说叫编码更为合适，因为不会直接在应用中展示这个名字")
    @Column(name = "code", nullable = false)
    private String code;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该模型是否是系统内置")
    @DefaultValue(Strings.FALSE)
    @Column(name = "built_in", nullable = false)
    private Boolean builtIn;

    @Title("同步标记")
    @Comment("同步标记用于标识在系统中展示的模型结构是否已经同步到数据库表当中")
    @DefaultValue(Strings.FALSE)
    @Column(name = "db_sync_flag", nullable = false)
    private Boolean syncFlag;

    @Title("备注")
    @Comment("模型备注对应数据库中表的注释，即表的Comment。对于应用内置的模型，备注中会有一段用于区分是否内置的标识。")
    @Column(name = "remark")
    private String remark;

    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示属性，与数据库中实际的顺序无关联")
    @Column(name = "order_num")
    private Integer orderNum;

    @Title("创建时间")
    @Comment("创建时间表示该数据模型新建的时间，类型为TimeStamp")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime;

    @Title("更新时间")
    @Comment("更新时间表示该数据模型最近一次修改的时间，类型为TimeStamp")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateTime;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @Title("创建人ID")
    @Comment("创建人ID为创建该数据模型的管理员的ID，使用外键关联")
    @JoinColumn(name = "created_by_id")
    private RvAdmin createdBy;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @Title("更新人ID")
    @Comment("更新人ID为最近一次更新该数据模型的管理员的ID，使用外键关联")
    @JoinColumn(name = "updated_by_id")
    private RvAdmin updatedBy;

    @JsonManagedReference("columns")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "prototype")
    private List<RvColumn> columns;

    @JsonSetter("columns")
    public void setColumns(List<RvColumn> columns) {
        this.columns = columns;
        if (columns == null) {
            return;
        }
        columns.forEach(column -> column.setPrototype(this));
    }

    @JsonManagedReference("indexes")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "prototype")
    private List<RvIndex> indexes;

    @JsonSetter("indexes")
    public void setIndexes(List<RvIndex> indexes) {
        this.indexes = indexes;
        if (indexes == null) {
            return;
        }
        indexes.forEach(index -> index.setPrototype(this));
    }

    @JsonManagedReference("primaryKey")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "prototype")
    private RvPrimaryKey primaryKey;

    @JsonSetter("primaryKey")
    public void setPrimaryKey(RvPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
        if (primaryKey == null) {
            return;
        }
        primaryKey.setPrototype(this);
    }

    @JsonManagedReference("foreignKeys")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "prototype")
    private List<RvForeignKey> foreignKeys;

    @JsonSetter("foreignKeys")
    public void setForeignKeys(List<RvForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
        if (foreignKeys == null) {
            return;
        }
        foreignKeys.forEach(foreignKey -> foreignKey.setPrototype(this));
    }

    @JsonManagedReference("uniques")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "prototype")
    private List<RvUnique> uniques;

    @JsonSetter("uniques")
    public void setUniques(List<RvUnique> uniques) {
        this.uniques = uniques;
        if (uniques == null) {
            return;
        }
        uniques.forEach(unique -> unique.setPrototype(this));
    }

    @JsonManagedReference("notNulls")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "prototype")
    private List<RvNotNull> notNulls;

    @JsonSetter("notNulls")
    public void setNotNulls(List<RvNotNull> notNulls) {
        this.notNulls = notNulls;
        if (notNulls == null) {
            return;
        }
        notNulls.forEach(notNull -> notNull.setPrototype(this));
    }
}
