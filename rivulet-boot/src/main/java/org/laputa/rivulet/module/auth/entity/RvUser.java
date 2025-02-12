package org.laputa.rivulet.module.auth.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.hibernate.validator.constraints.Length;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.common.validation.RvValidationGroup;
import org.laputa.rivulet.module.auth.entity.dict.UserType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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
@Table(name = "rv_user", uniqueConstraints = {
        @UniqueConstraint(name = "uc_rvuser_username", columnNames = {"username"})
}, indexes = {
        @Index(name = "idx_rvuser_usertype", columnList = "user_type")
})
public class RvUser extends RvEntity {
    @NotNull(groups = Update.class)
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", nullable = false, length = 32)
    private String id;

    @NotNull(groups = {Persist.class, Login.class})
    @Length(min = 2, max = 32)
    @Comment("用户名")
    @Column(name = "username", nullable = false, length = 32, unique = true)
    private String username;

    @NotNull(groups = {Persist.class, Login.class})
    @Pattern(regexp = "[0-9A-Za-z]{32}|[0-9A-Za-z]{64}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Comment("用户密码，前端传入时为用户输入密码的MD5值，长度为固定的32位，" +
            "加盐合并之后，长度为固定的64位，即前端传入值和数据库存储值长度不同")
    @Column(name = "password", nullable = false, length = 64)
    private String password;

    @Comment("用户类别")
    @Convert(converter = UserType.Converter.class)
    @Column(name = "user_type", nullable = false, precision = 1)
    private UserType userType;

    @PrePersist
    private void onPrePersist() {
        // 用户类别默认是普通用户
        if (this.userType == null) {
            this.userType = UserType.NORMAL_USER;
        }
    }

    public interface Login extends RvValidationGroup {
    }
}
