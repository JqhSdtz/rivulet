package org.laputa.rivulet.common.util;

import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import org.laputa.rivulet.module.app.property.TerminalKeyProperty;
import org.redisson.api.RBucket;
import org.springframework.stereotype.Component;

/**
 * 产生一个终端密钥，存放于Redis中
 * @author JQH
 * @since 下午 10:28 22/10/22
 */
@Component
public class TerminalKeyUtil {
    @Resource
    private TerminalKeyProperty terminalKeyProperty;

    public String generateTerminalKey(RBucket<String> keyBucket) {
        // 获取Redis中的初始密钥
        String initKey = keyBucket.get();
        // 如果没有，则创建一个初始密钥，并尝试设置Redis中的对应值
        if (initKey == null) {
            String tmpKey = RandomUtil.randomString(terminalKeyProperty.getRandomBase(), terminalKeyProperty.getLength());
            // 可能存在并发问题，所以使用trySet
            if (!keyBucket.trySet(tmpKey, terminalKeyProperty.getTimeout(), terminalKeyProperty.getTimeUnit())) {
                tmpKey = keyBucket.get();
            }
            initKey = tmpKey;
        } else {
            // 如果有了，则刷新过期时间
            keyBucket.expire(TimeUnitUtil.toDuration(terminalKeyProperty.getTimeout(), terminalKeyProperty.getTimeUnit()));
        }
        return initKey;
    }
}
