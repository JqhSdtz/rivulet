package org.laputa.rivulet.module.jpa_model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvBaseEntity;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.laputa.rivulet.module.dbms_model.entity.RvColumn;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
@Cache(region = "defaultCache", usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@DynamicInsert
@DynamicUpdate
@Title("数据模型")
@TableComment("数据模型和JPA中的实体类对应，即为实体类建模")
@Table(name = "rv_prototype")
public class RvPrototype extends RvBaseEntity<String> {
    @Id
    @UuidGenerator
    @Title("模型ID")
    @Comment("模型ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Title("模型名称")
    @Comment("模型名称在数据库中并不存在对应的内容，是为了便于中文展示而单独设置的")
    @Column(name = "title", nullable = false)
    private String title;

    @Title("模型编码")
    @Comment("模型编码对应数据库中表的名称，也是实体类的类名。数据库中使用下划线命名，实体类使用JAVA类的命名规范。")
    @Column(name = "code", nullable = false)
    private String code;

    @Title("同步标记")
    @Comment("同步标记用于标识在系统中展示的模型结构是否已经同步到hibernate的metadata当中")
    @DefaultValue(Strings.FALSE)
    @Column(name = "sync_flag", nullable = false)
    private Boolean syncFlag;

    @Title("备注")
    @Comment("模型备注用于对模型进行解释说明")
    @Column(name = "remark")
    private String remark;

    @Title("排序号")
    @Comment("排序号用于按设定的顺序展示属性")
    @Column(name = "order_num")
    private Integer orderNum;

    @Title("创建时间")
    @Comment("创建时间表示该数据模型新建的时间，类型为TimeStamp")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime;

    @Title("更新时间")
    @Comment("更新时间表示该数据模型最近一次修改的时间，类型为TimeStamp")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateTime;

    @ManyToOne(cascade = CascadeType.MERGE)
    @Cache(region = "defaultCache", usage = CacheConcurrencyStrategy.READ_WRITE)
    @Title("创建人ID")
    @Comment("创建人ID为创建该数据模型的管理员的ID，使用外键关联")
    @JoinColumn(name = "created_by_id")
    private RvAdmin createdBy;

    @ManyToOne(cascade = CascadeType.MERGE)
    @Cache(region = "defaultCache", usage = CacheConcurrencyStrategy.READ_WRITE)
    @Title("更新人ID")
    @Comment("更新人ID为最近一次更新该数据模型的管理员的ID，使用外键关联")
    @JoinColumn(name = "updated_by_id")
    private RvAdmin updatedBy;

    @Cache(region = "defaultCache", usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonManagedReference("properties")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "prototype")
    private List<RvProperty> properties;

    @JsonSetter("properties")
    public void setProperties(List<RvProperty> properties) {
        if (properties == null) {
            return;
        }
        if (this.properties == null) {
            this.properties = properties;
        } else {
            this.properties.clear();
            this.properties.addAll(properties);
        }
        properties.forEach(property -> property.setPrototype(this));
    }
}
