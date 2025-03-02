package org.laputa.rivulet.module.app.controller;

import jakarta.annotation.Resource;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.script.JsRunner;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/js")
public class JsController {
    @Resource
    private JsRunner jsRunner;

    @GetMapping("/run")
    public Result<Object> getModelDetailSchema(@RequestParam String filename) {
        return jsRunner.runScript(filename);
    }

    @PostMapping("/clearCache")
    public Result<Void> clearCache() {
        return jsRunner.clearCache();
    }
}
