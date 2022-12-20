package org.laputa.rivulet.module.data_model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvPrimaryKeyColumn;

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
@Table(name = "rv_primary_key", indexes = {
        @Index(name = "idx_rvprimarykey_prototype_id", columnList = "prototype_id")
})
public class RvPrimaryKey extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("primaryKey")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "name", nullable = false)
    private String name;

    @JsonManagedReference("primaryKeyColumns")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "primaryKey")
    private List<RvPrimaryKeyColumn> primaryKeyColumns;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "backing_index_id")
    private RvIndex backingIndex;

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;

    @JsonSetter("primaryKeyColumns")
    public void setPrimaryKeyColumns(List<RvPrimaryKeyColumn> primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
        if (primaryKeyColumns == null) {
            return;
        }
        primaryKeyColumns.forEach(primaryKeyColumn -> primaryKeyColumn.setPrimaryKey(this));
    }
}
