package org.laputa.rivulet.common.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author JQH
 * @since 下午 12:48 22/04/03
 */
public class TimeUnitUtil {
    public static String format(long value, TimeUnit timeUnit) {
        String unitStr;
        if (timeUnit == MILLISECONDS) {
            unitStr = "毫秒";
        } else if (timeUnit == SECONDS) {
            unitStr = "秒";
        } else if (timeUnit == MINUTES) {
            unitStr = "分钟";
        } else if (timeUnit == HOURS) {
            unitStr = "小时";
        } else if (timeUnit == DAYS) {
            unitStr = "天";
        } else {
            unitStr = timeUnit.toString();
        }
        return value + unitStr;
    }

    public static Duration toDuration(long value, TimeUnit timeUnit) {
        return Duration.ofMillis(timeUnit.toMillis(value));
    }
}
