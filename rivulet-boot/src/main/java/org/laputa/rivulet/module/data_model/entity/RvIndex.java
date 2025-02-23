package org.laputa.rivulet.module.data_model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.data_model.entity.column_relation.RvIndexColumn;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.laputa.rivulet.module.data_model.entity.inter.DataModelEntityInterface;

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
@Title("数据模型索引")
@TableComment("数据模型索引和数据库的表索引对应，一般创建主键、外键以及唯一性约束时会自动生成，也可以自己添加。被索引的列有更快的查询速度。")
@Table(name = "rv_index")
public class RvIndex extends RvEntity<String> implements DataModelEntityInterface {
    @Id
    @UuidGenerator
    @Title("索引ID")
    @Comment("索引ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("indexes")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @Title("对应模型ID")
    @Comment("索引所对应的数据模型的ID，使用外键关联")
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Title("系统内置")
    @Comment("系统内置标记用于标记该索引是否是系统内置")
    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @Title("索引名称")
    @Comment("索引名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("索引编码")
    @Comment("索引编码对应数据库中索引的名称，即索引的Name")
    @Column(name = "code", nullable = false)
    private String code;

    @Title("唯一索引")
    @Comment("唯一索引标记用于标识该索引是否是唯一索引")
    @Column(name = "unique_index")
    private Boolean uniqueIndex;

    @Title("备注")
    @Comment("索引备注对应数据库中索引的注释，即索引的Comment")
    @Column(name = "remark")
    private String remark;

    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示索引，与数据库中实际的顺序无关联")
    @Column(name = "order_num")
    private Integer orderNum;

    @JsonManagedReference("indexColumns")
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

}
