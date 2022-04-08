package org.laputa.rivulet.module.app.controller;

import org.laputa.rivulet.common.entity.RvEntity;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.app.model.AppInitialData;
import org.laputa.rivulet.module.app.service.AppService;
import org.laputa.rivulet.module.auth.entity.RvUser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author JQH
 * @since 下午 9:45 22/03/30
 */
@RestController
@RequestMapping("/api/app")
public class AppController {
    @Resource
    private AppService appService;

    @GetMapping("/initialData")
    public AppInitialData getAppInitialData() {
        return appService.getAppInitialData();
    }

    @PostMapping("/verifyInitKey")
    public Result<Void> verifyInitKey(@RequestBody Map<String, String> params) {
        String initKey = params.get("initKey");
        boolean isValid = appService.verifyInitKey(initKey);
        return Result.empty(isValid);
    }

    @PostMapping("/initialUser")
    public Result<Void> createInitialUser(@RequestBody @Validated(RvEntity.Persist.class) RvUser rvUser) {
        return appService.createInitialUser(rvUser);
    }
}
