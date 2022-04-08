package org.laputa.rivulet.module.app.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author JQH
 * @since 下午 8:58 22/04/02
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "rivulet.app.init-key")
public class InitKeyProperty {
    /**
     * 用于产生出事密钥的随机字符串的字符集
     */
    private String randomBase = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 初始密钥的长度
     */
    private int length = 32;
    /**
     * 初始密钥的失效时间
     */
    private long timeout = 10;
    /**
     * 初始密钥失效时间单位
     */
    private TimeUnit timeUnit = TimeUnit.MINUTES;
}
