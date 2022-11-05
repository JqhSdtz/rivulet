package org.laputa.rivulet.module.data_model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvEntity;

import javax.persistence.*;
import java.util.List;

/**
 * @author JQH
 * @since 下午 8:17 22/01/30
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@TableComment("数据模型")
@Table(name = "rv_prototype")
public class RvPrototype extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "remark")
    private String remark;

    @Column(name = "db_sync_flag", nullable = false)
    @DefaultValue(Strings.FALSE)
    private boolean dbSyncFlag;

    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private boolean builtIn;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prototype")
    private List<RvColumn> columns;

    @JsonSetter("columns")
    public void setColumns(List<RvColumn> columns) {
        this.columns = columns;
        if (columns == null) {
            return;
        }
        columns.forEach(column -> column.setPrototype(this));
    }

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prototype")
    private List<RvIndex> indexes;

    @JsonSetter("indexes")
    public void setIndexes(List<RvIndex> indexes) {
        this.indexes = indexes;
        if (indexes == null) {
            return;
        }
        indexes.forEach(index -> index.setPrototype(this));
    }

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "prototype")
    private RvPrimaryKey primaryKey;

    @JsonSetter("primaryKey")
    public void setPrimaryKey(RvPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
        if (primaryKey == null) {
            return;
        }
        primaryKey.setPrototype(this);
    }

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prototype")
    private List<RvForeignKey> foreignKeys;

    @JsonSetter("foreignKeys")
    public void setForeignKeys(List<RvForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
        if (foreignKeys == null) {
            return;
        }
        foreignKeys.forEach(foreignKey -> foreignKey.setPrototype(this));
    }

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prototype")
    private List<RvUniqueConstraint> uniqueConstraints;

    @JsonSetter("uniqueConstraints")
    public void setUniqueConstraints(List<RvUniqueConstraint> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
        if (uniqueConstraints == null) {
            return;
        }
        uniqueConstraints.forEach(uniqueConstraint -> uniqueConstraint.setPrototype(this));
    }
}
