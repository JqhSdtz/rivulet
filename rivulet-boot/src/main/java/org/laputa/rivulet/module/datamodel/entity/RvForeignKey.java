package org.laputa.rivulet.module.datamodel.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.datamodel.entity.column_relation.RvForeignKeyForeignColumn;
import org.laputa.rivulet.module.datamodel.entity.column_relation.RvForeignKeyLocalColumn;
import org.laputa.rivulet.module.datamodel.entity.column_relation.RvPrimaryKeyColumn;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.*;
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
@DynamicInsert
@DynamicUpdate
@Table(name = "rv_foreign_key", indexes = {
        @Index(name = "idx_rvforeignkey_prototype_id", columnList = "prototype_id")
})
public class RvForeignKey extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "cascade_delete")
    private Boolean cascadeDelete;

    @OneToOne
    @JoinColumn(name = "backing_index_id")
    private RvIndex backingIndex;

    @JsonBackReference
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    @JoinColumn(name = "foreign_prototype_id")
    private RvPrototype foreignPrototype;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "foreignKey")
    private List<RvForeignKeyLocalColumn> foreignKeyLocalColumns;

    @JsonSetter("foreignKeyLocalColumns")
    public void setForeignKeyLocalColumns(List<RvForeignKeyLocalColumn> foreignKeyLocalColumns) {
        this.foreignKeyLocalColumns = foreignKeyLocalColumns;
        if (foreignKeyLocalColumns == null) {
            return;
        }
        foreignKeyLocalColumns.forEach(foreignKeyLocalColumn -> foreignKeyLocalColumn.setForeignKey(this));
    }

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "foreignKey")
    private List<RvForeignKeyForeignColumn> foreignKeyForeignColumns;

    @JsonSetter("foreignKeyForeignColumns")
    public void setForeignKeyForeignColumns(List<RvForeignKeyForeignColumn> foreignKeyForeignColumns) {
        this.foreignKeyForeignColumns = foreignKeyForeignColumns;
        if (foreignKeyForeignColumns == null) {
            return;
        }
        foreignKeyForeignColumns.forEach(foreignKeyForeignColumn -> foreignKeyForeignColumn.setForeignKey(this));
    }

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;

}
