package org.laputa.rivulet.module.dict.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "rv_local_test")
public class RvLocalTest {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "val", nullable = false, length = 64)
    private String val;
}
