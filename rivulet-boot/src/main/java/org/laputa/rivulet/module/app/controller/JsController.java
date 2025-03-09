package org.laputa.rivulet.module.app.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.script.JsRunner;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/js")
public class JsController {
    @Resource
    private JsRunner jsRunner;

    @PostMapping("/run")
    public Result<Object> runScript(@RequestParam String filename, @RequestBody String jsonStr, HttpServletRequest request) {
        request.setAttribute("jsonStr", jsonStr);
        return jsRunner.runScript(filename);
    }

    @PostMapping("/runWithTransaction")
    public Result<Object> runScriptWithTransaction(@RequestParam String filename, @RequestBody String jsonStr, HttpServletRequest request) {
        request.setAttribute("jsonStr", jsonStr);
        return jsRunner.runScriptWithTransaction(filename);
    }

    @PostMapping("/clearCache")
    public Result<Void> clearCache() {
        return jsRunner.clearCache();
    }
}
