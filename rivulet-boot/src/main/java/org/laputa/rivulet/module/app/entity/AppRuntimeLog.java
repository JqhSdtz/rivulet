package org.laputa.rivulet.module.app.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.laputa.rivulet.common.entity.RvBaseEntity;

import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
@Cache(region = "defaultCache", usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicInsert
@DynamicUpdate
@Title("系统运行日志")
@TableComment("用于存储系统运行过程中产生的日志，可通过日志服务页面查看日志内容")
@Table(name = "rv_runtime_log")
public class AppRuntimeLog extends RvBaseEntity<String> {
    @Id
    @UuidGenerator
    @Title("日志ID")
    @Column(name = "id", nullable = false)
    private String id;

    @Title("来源")
    @Comment("日志来源，可能是某个系统，也可能是某个系统的某个用户")
    @Column(name = "source")
    private String source;

    @Title("日志级别")
    @Column(name = "level")
    private String level;

    @Title("日志内容")
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Title("记录时间")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "log_time")
    private Date logTime;
}
