package org.laputa.rivulet.module.data_model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import liquibase.ext.hibernate.annotation.TableComment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvIndexColumn;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
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
@TableComment("数据模型索引")
@Table(name = "rv_index", indexes = {
        @Index(name = "idx_rvindex_prototype_id", columnList = "prototype_id")
})
public class RvIndex extends RvEntity<String> {
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

    @Column(name = "unique_index")
    private Boolean uniqueIndex;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "index")
    private List<RvIndexColumn> indexColumns;

    @JsonSetter("indexColumns")
    public void setIndexColumns(List<RvIndexColumn> indexColumns) {
        this.indexColumns = indexColumns;
        if (indexColumns == null) {
            return;
        }
        indexColumns.forEach(indexColumn -> indexColumn.setIndex(this));
    }

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;
}
