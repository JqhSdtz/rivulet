package org.laputa.rivulet.module.dict.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.common.entity.RvTree;

import jakarta.persistence.*;

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
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
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
