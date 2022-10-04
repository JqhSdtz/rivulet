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

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "index")
    private List<RvIndexColumn> indexColumns;

    @Column(name = "remark")
    private String remark;

    @Column(name = "order")
    private Integer order;

    @JsonSetter("indexColumns")
    public void setIndexColumns(List<RvIndexColumn> indexColumns) {
        this.indexColumns = indexColumns;
        if (indexColumns == null) {
            return;
        }
        indexColumns.forEach(indexColumn -> indexColumn.setIndex(this));
    }
}
