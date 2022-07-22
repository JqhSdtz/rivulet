package org.laputa.rivulet.module.dict.service;

import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.common.constant.Global;
import org.laputa.rivulet.module.dict.entity.RvDict;
import org.laputa.rivulet.module.dict.repository.RvDictRepository;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO 建立字典分布式同步机制
 * @author JQH
 * @since 下午 5:26 22/07/20
 */
@Service
@Slf4j
public class DictService implements ApplicationRunner {

    @Resource
    private RvDictRepository rvDictRepository;
    @Resource
    private RedissonClient redissonClient;

    private RReadWriteLock dictReadWriteLock;

    private RMap remoteDictMap;

    private Map localDictMap = new HashMap<>(16);;

    @PostConstruct
    private void postConstruct() {
        remoteDictMap = redissonClient.getMap("dictMap");
        dictReadWriteLock = redissonClient.getReadWriteLock("dictReadWriteLock");
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        writeDict: if (!remoteDictMap.isExists()) {
            // 如果Redis中不存在字典，则创建，创建时加写锁
            RLock writeDictLock = dictReadWriteLock.writeLock();
            if (writeDictLock.tryLock(Global.LOCK_WAIT_TIME, Global.LOCK_WAIT_TIME_UNIT)) {
                try {
                    if (remoteDictMap.isExists()) {
                        log.info("字典缓存已存在");
                        break writeDict;
                    }
                    List<RvDict> allDictList = rvDictRepository.findAll();
                    allDictList.forEach(dict -> {
                        localDictMap.put(dict.getId(), dict);
                    });
                    remoteDictMap.putAll(localDictMap);
                } finally {
                    writeDictLock.unlock();
                }
            } else {
                throw new RuntimeException("获取字典缓存写锁失败。启动失败！");
            }
        } else {
            log.info("字典缓存已存在");
        }
    }
}
