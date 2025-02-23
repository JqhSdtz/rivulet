package org.laputa.rivulet.module.data_model.entity.constraint;

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
import org.laputa.rivulet.module.data_model.entity.RvPrototype;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.laputa.rivulet.module.data_model.entity.inter.WithBuiltInFlag;

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
@Table(name = "rv_not_null")
public class RvNotNull extends RvEntity<String> implements WithBuiltInFlag {
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

    @Column(name = "built_in", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean builtIn;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "column_id")
    private RvColumn column;

    @Column(name = "remark")
    private String remark;

    @Column(name = "order_num")
    private Integer orderNum;
}
