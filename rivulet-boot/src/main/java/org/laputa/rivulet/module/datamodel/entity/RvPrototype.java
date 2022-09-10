package org.laputa.rivulet.module.datamodel.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.laputa.rivulet.common.entity.RvEntity;

import javax.persistence.*;
import java.util.List;

/**
 * @author JQH
 * @since 下午 8:17 22/01/30
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "rv_prototype")
public class RvPrototype extends RvEntity<String> {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false, columnDefinition = "java.sql.Types.VARCHAR(77)")
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "db_sync_flag", nullable = false)
    private boolean dbSyncFlag;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prototype")
    private List<RvField> fields;

    @JsonSetter("fields")
    public void setFields(List<RvField> fields) {
        this.fields = fields;
        if (fields == null) {
            return;
        }
        fields.forEach(field -> field.setPrototype(this));
    }
}
