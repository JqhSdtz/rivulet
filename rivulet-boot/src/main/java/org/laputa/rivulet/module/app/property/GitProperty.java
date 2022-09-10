package org.laputa.rivulet.module.app.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 4:03 22/09/04
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "rivulet.app.git")
public class GitProperty {
    /**
     * git仓库地址
     */
    private String repoUrl;

    /**
     * 本地保存代码的位置
     */
    private String localDir;

    /**
     * git仓库访问用户名
     */
    private String username;

    /**
     * git仓库访问密码
     */
    private String password;
}
