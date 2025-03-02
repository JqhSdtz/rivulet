package org.laputa.rivulet.module.app.controller;

import jakarta.annotation.Resource;
import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.app.model.AppInitialData;
import org.laputa.rivulet.module.app.service.AppInitService;
import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author JQH
 * @since 下午 9:45 22/03/30
 */
@RestController
@RequestMapping("/api/app")
public class AppController {
    @Resource
    private AppInitService appService;

    @GetMapping("/initialData")
    public Result<AppInitialData> getAppInitialData() {
        return appService.getAppInitialData();
    }

    @PostMapping("/verifyInitKey")
    public Result<Void> verifyInitKey(@RequestBody Map<String, String> params) {
        String initKey = params.get("initKey");
        boolean isValid = appService.verifyInitKey(initKey);
        return Result.empty(isValid);
    }

    @PostMapping("/initialAdmin")
    public Result<Void> createInitialAdmin(@RequestBody @Validated(RvEntity.Persist.class) RvAdmin rvAdmin) {
        return appService.createInitialAdmin(rvAdmin);
    }
}
