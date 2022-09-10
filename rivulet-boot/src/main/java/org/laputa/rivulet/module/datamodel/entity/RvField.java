package org.laputa.rivulet.module.datamodel.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.laputa.rivulet.common.entity.RvEntity;

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
@Table(name = "rv_field", indexes = {
        @Index(name = "idx_rvfield_prototype_id", columnList = "prototype_id")
})
public class RvField extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    /**
     * 这里的@JoinColumn的nullable属性不能设为false，否则无法正确插入数据
     */
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "prototype_id")
    private RvPrototype prototype;

    @Column(name = "name", nullable = false, columnDefinition = "java.sql.Types.VARCHAR(77)")
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "data_type", nullable = false)
    private String dataType;
}
