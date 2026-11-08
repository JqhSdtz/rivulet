package org.laputa.rivulet.module.app.service;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.app.entity.AppRuntimeLog;
import org.laputa.rivulet.module.app.repository.AppRuntimeLogRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AppRuntimeLogService {
    @Resource
    private AppRuntimeLogRepository appRuntimeLogRepository;

    public Result<Void> addLog(String source, String level, String content) {
        AppRuntimeLog appRuntimeLog = new AppRuntimeLog();
        appRuntimeLog.setSource(source);
        appRuntimeLog.setLevel(level);
        appRuntimeLog.setContent(content);
        appRuntimeLog.setLogTime(new Date());
        appRuntimeLogRepository.save(appRuntimeLog);
        return Result.succeed();
    }

    public Result<List<AppRuntimeLog>> queryLog(int queryNum) {
        return Result.succeed(appRuntimeLogRepository.findByOrderByLogTimeDesc(Limit.of(queryNum)));
    }
}
