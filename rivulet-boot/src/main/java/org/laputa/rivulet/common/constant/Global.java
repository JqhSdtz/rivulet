package org.laputa.rivulet.common.constant;

import java.util.concurrent.TimeUnit;

/**
 * @author JQH
 * @since 下午 11:17 22/07/21
 */
public class Global {
    /**
     * tryLock的等待时间
     */
    public static long LOCK_WAIT_TIME = 20L;

    /**
     * tryLock的等待时间的单位
     */
    public static TimeUnit LOCK_WAIT_TIME_UNIT = TimeUnit.SECONDS;
}
