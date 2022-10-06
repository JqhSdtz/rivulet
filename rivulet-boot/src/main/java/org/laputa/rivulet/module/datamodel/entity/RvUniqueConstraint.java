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
import org.laputa.rivulet.module.datamodel.entity.column_relation.RvIndexColumn;
import org.laputa.rivulet.module.datamodel.entity.column_relation.RvUniqueConstraintColumn;

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
@Table(name = "rv_unique_constraint", indexes = {
        @Index(name = "idx_rvuniqueconstraint_prototype_id", columnList = "prototype_id")
})
public class RvUniqueConstraint extends RvEntity<String> {
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

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "backing_index_id")
    private RvIndex backingIndex;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "uniqueConstraint")
    private List<RvUniqueConstraintColumn> uniqueConstraintColumns;

    @JsonSetter("uniqueConstraintColumns")
    public void setUniqueConstraintColumns(List<RvUniqueConstraintColumn> uniqueConstraintColumns) {
        this.uniqueConstraintColumns = uniqueConstraintColumns;
        if (uniqueConstraintColumns == null) {
            return;
        }
        uniqueConstraintColumns.forEach(uniqueConstraintColumn -> uniqueConstraintColumn.setUniqueConstraint(this));
    }

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;
}
