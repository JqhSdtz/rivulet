package org.laputa.rivulet.common.util;

import jakarta.annotation.Resource;
import org.laputa.rivulet.common.model.Result;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JQH
 * @since 上午 11:44 22/10/02
 */
@Component
public class RedissonLockUtil {
    @Resource
    private RedissonClient redissonClient;
    private Map<String, RLock> lockMap = new HashMap<>();
    public interface DoInLockInterface {
        Result doInLock();
    }
    public Result doWithLock(String name, DoInLockInterface doInLockInterface) {
        Result failResult = Result.fail("lockWaitFail", "分布式锁等待超时");
        return doWithLock(name, doInLockInterface, failResult);
    }
    public Result doWithLock(String name, DoInLockInterface doInLockInterface, Result failResult) {
        RLock lock = lockMap.get(name);
        if (lock == null) {
            lock = redissonClient.getLock(name);
            lockMap.put(name, lock);
        }
        if (lock.tryLock()) {
            try {
                try {
                    return doInLockInterface.doInLock();
                } catch (Exception exception) {
                    failResult.setRawException(exception);
                    return failResult;
                }
            } finally {
                lock.unlock();
            }
        } else {
            return failResult;
        }
    }
}
