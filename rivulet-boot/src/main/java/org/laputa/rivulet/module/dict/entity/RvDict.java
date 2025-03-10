package org.laputa.rivulet.module.dict.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.laputa.rivulet.common.entity.RvTree;

/**
 * @author JQH
 * @since 下午 9:51 22/07/19
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "rv_dict")
public class RvDict extends RvTree<String> {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "parent_id", length = 64)
    private String parentId;

    @Comment("字典编码")
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Comment("字典内容")
    @Column(name = "text", nullable = false)
    private String text;
}
