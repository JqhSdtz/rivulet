package org.laputa.rivulet.module.data_model.entity.constraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.data_model.entity.RvIndex;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvForeignKeyForeignColumn;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvForeignKeyTargetColumn;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.*;
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
@Table(name = "rv_foreign_key")
public class RvForeignKey extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("foreignKeys")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cascade_delete")
    private Boolean cascadeDelete;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "backing_index_id")
    private RvIndex backingIndex;

    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "target_prototype_id")
    private RvPrototype targetPrototype;

    @JsonManagedReference("foreignKeyTargetColumns")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "foreignKey")
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
