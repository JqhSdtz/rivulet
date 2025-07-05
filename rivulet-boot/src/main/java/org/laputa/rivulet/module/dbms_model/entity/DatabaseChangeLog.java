package org.laputa.rivulet.module.dbms_model.entity;

import jakarta.persistence.*;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.Getter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.UuidGenerator;
import org.laputa.rivulet.common.entity.RvBaseEntity;
import org.laputa.rivulet.module.auth.entity.RvAdmin;

import java.util.Date;

@Entity
@Getter
@Title("数据库变更记录")
@TableComment("数据库变更记录由Liquibase框架自动创建和维护")
@Table(name = "databasechangelog", indexes = {
        @Index(name = "idx_databasechangelog_author", columnList = "author")
})
public class DatabaseChangeLog extends RvBaseEntity<String> {
    @Id
    @UuidGenerator
    @Title("变更记录ID")
    @Comment("变更记录ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @Title("创建者ID")
    @Comment("创建者ID为创建该变更记录的管理员的ID，不使用外键关联，因为系统初始化时需要将author设为初始管理员，但是此时并没有rv_admin这个表，所以有外键约束会导致无法插入数据")
    @JoinColumn(name = "author", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private RvAdmin author;

    @Title("文件名")
    @Comment("变更记录存储的文件名")
    @Column(name = "filename", nullable = false)
    private String filename;

    @Title("执行时间")
    @Comment("变更记录执行的时间")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dateexecuted", nullable = false)
    private Date dateExecuted;

    @Title("执行顺序")
    @Comment("变更记录执行的顺序")
    @Column(name = "orderexecuted", nullable = false)
    private Integer orderExecuted;

    @Title("执行类型")
    @Comment("变更记录的执行类型")
    @Column(name = "exectype", nullable = false)
    private String execType;

    @Title("MD5值")
    @Comment("变更记录的MD5值")
    @Column(name = "md5sum")
    private String md5Sum;

    @Title("内容")
    @Comment("变更记录的具体内容，包括变更类型和变更数据")
    @Column(name = "description")
    private String description;

    @Title("备注")
    @Comment("变更记录的备注")
    @Column(name = "comments")
    private String comments;

    @Title("标签")
    @Comment("变更记录的标签")
    @Column(name = "tag")
    private String tag;

    @Title("Liquibase版本")
    @Comment("创建变更记录时的Liquibase版本")
    @Column(name = "liquibase")
    private String liquibase;

    @Title("上下文")
    @Comment("变更记录的上下文")
    @Column(name = "contexts")
    private String contexts;

    @Title("标志")
    @Comment("变更记录的标志")
    @Column(name = "labels")
    private String labels;

    @Title("部署ID")
    @Comment("变更记录的部署ID")
    @Column(name = "deployment_id")
    private String deploymentId;
}
