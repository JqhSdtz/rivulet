package org.laputa.rivulet.module.datamodel.entity;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.laputa.rivulet.common.entity.RvEntity;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author JQH
 * @since 下午 8:17 22/01/30
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "rv_table")
public class RvTable extends RvEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;
}
