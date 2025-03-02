package org.laputa.rivulet.module.auth.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import liquibase.ext.hibernate.annotation.DefaultValue;
import liquibase.ext.hibernate.annotation.TableComment;
import liquibase.ext.hibernate.annotation.Title;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.common.validation.RvValidationGroup;
import org.laputa.rivulet.module.auth.entity.dict.AdminType;

/**
 * @author JQH
 * @since 下午 9:23 22/03/10
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@DynamicInsert
@DynamicUpdate
@Title("系统管理员")
@TableComment("数据模型和数据库表对应，包含属性、索引、外键等，用于描述一个结构化的数据")
@Table(name = "rv_admin", uniqueConstraints = {
        @UniqueConstraint(name = "uc_rvadmin_adminname", columnNames = {"admin_name"})
}, indexes = {
        @Index(name = "idx_rvadmin_admintype", columnList = "admin_type")
})
public class RvAdmin extends RvEntity<String> {
    @NotNull(groups = Update.class)
    @Id
    @RvAdminIdentityGenerator
    @Title("管理员ID")
    @Comment("管理员ID使用UUID策略，生成的ID绝对唯一")
    @Column(name = "id", nullable = false)
    private String id;

    @NotNull(groups = {Persist.class, Login.class})
    @Length(min = 2, max = 32)
    @Title("管理员登录名")
    @Comment("管理员登录名为管理员登录时输入的用户名")
    @Column(name = "admin_name", nullable = false, unique = true)
    private String adminName;

    @NotNull(groups = {Persist.class, Login.class})
    @Pattern(regexp = "[0-9A-Za-z]{32}|[0-9A-Za-z]{64}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Title("管理员密码")
    @Comment("管理员密码，前端传入时为管理员输入密码的MD5值，长度为固定的32位，" +
            "加盐合并之后，长度为固定的64位，即前端传入值和数据库存储值长度不同")
    @Column(name = "password", nullable = false, length = 64)
    private String password;

    @Title("管理员类别")
    @Comment("管理员类别当前分为：0.初始管理员，初始化应用的时候生成，拥有最高权限；1.普通管理员，由初始管理员在系统中添加")
    @Convert(converter = AdminType.Converter.class)
    @Column(name = "admin_type", nullable = false, precision = 1)
    private AdminType adminType;

    @Title("是否激活")
    @Comment("初始用户在没设置密码前是未激活状态")
    @Column(name = "active", nullable = false)
    @DefaultValue(Strings.FALSE)
    private Boolean active;

    @PrePersist
    private void onPrePersist() {
        // 用户类别默认是普通用户
        if (this.adminType == null) {
            this.adminType = AdminType.NORMAL_ADMIN;
        }
    }

    public interface Login extends RvValidationGroup {
    }
}
