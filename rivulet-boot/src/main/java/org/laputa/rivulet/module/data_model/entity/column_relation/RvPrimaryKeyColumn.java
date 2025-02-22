package org.laputa.rivulet.module.data_model.entity.column_relation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import liquibase.ext.hibernate.annotation.DefaultValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.module.data_model.entity.RvColumn;
import org.laputa.rivulet.module.data_model.entity.constraint.RvPrimaryKey;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import org.laputa.rivulet.module.data_model.entity.inter.WithBuiltInFlag;

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
@Table(name = "rv_primary_key_column")
public class RvPrimaryKeyColumn extends RvEntity<String> implements WithBuiltInFlag {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference("primaryKeyColumns")
    @ToString.Exclude
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "primary_key_id")
    private RvPrimaryKey primaryKey;

    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "column_id")
    private RvColumn column;

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;
}
