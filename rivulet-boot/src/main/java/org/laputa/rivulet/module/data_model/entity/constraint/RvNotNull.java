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
import org.laputa.rivulet.module.data_model.entity.column_relation.RvNotNullColumn;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvUniqueColumn;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.List;

/**
 * @author JQH
 * @since 下午 7:06 23/04/15
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "rv_not_null", indexes = {
        @Index(name = "idx_rvnotnull_prototype_id", columnList = "prototype_id")
})
public class RvNotNull extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @JsonBackReference("notNulls")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "name", nullable = false)
    private String name;

    @JsonManagedReference("notNullColumns")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "notNull")
    private List<RvNotNullColumn> notNullColumns;

    @JsonSetter("notNullColumns")
    public void setNotNullColumns(List<RvNotNullColumn> rvNotNullColumns) {
        this.notNullColumns = rvNotNullColumns;
        if (rvNotNullColumns == null) {
            return;
        }
        rvNotNullColumns.forEach(rvNotNullColumn -> rvNotNullColumn.setNotNull(this));
    }

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "backing_index_id")
    private RvIndex backingIndex;

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;
}
