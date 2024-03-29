package org.laputa.rivulet.module.data_model.entity.column_relation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.data_model.entity.RvColumn;
import org.laputa.rivulet.module.data_model.entity.RvIndex;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

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
@Table(name = "rv_index_column", indexes = {
        @Index(name = "idx_rvindexcolumn_index_id", columnList = "index_id"),
        @Index(name = "idx_rvindexcolumn_column_id", columnList = "column_id")
})
public class RvIndexColumn extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("indexColumns")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "index_id")
    private RvIndex index;

    @OneToOne(cascade = CascadeType.PERSIST)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "column_id")
    private RvColumn column;

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;
}
