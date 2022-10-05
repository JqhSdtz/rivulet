package org.laputa.rivulet.module.datamodel.entity.column_relation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.datamodel.entity.RvColumn;
import org.laputa.rivulet.module.datamodel.entity.RvIndex;
import org.laputa.rivulet.module.datamodel.entity.RvUniqueConstraint;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.*;

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
@Table(name = "rv_unique_constraint_column", indexes = {
        @Index(name = "idx_rvuniqueconstraintcolumn_constraint_id", columnList = "unique_constraint_id"),
        @Index(name = "idx_rvuniqueconstraintcolumn_column_id", columnList = "column_id")
})
public class RvUniqueConstraintColumn extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "unique_constraint_id")
    private RvUniqueConstraint uniqueConstraint;

    @OneToOne
    @JoinColumn(name = "column_id")
    private RvColumn column;

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;
}
