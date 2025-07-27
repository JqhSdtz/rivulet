package org.laputa.rivulet.module.app.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.laputa.rivulet.common.constant.Strings;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.script.JsRunner;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/js")
public class JsController {
    @Resource
    private JsRunner jsRunner;

    @PostMapping("/run")
    public Result<?> runScript(@RequestParam String filename, @RequestBody String requestBodyStr, HttpServletRequest request) {
        request.setAttribute(Strings.RequestBodyStr, requestBodyStr);
        return jsRunner.runScript(filename);
    }

    @PostMapping("/runWithTransaction")
    public Result<?> runScriptWithTransaction(@RequestParam String filename, @RequestBody String requestBodyStr, HttpServletRequest request) {
        request.setAttribute(Strings.RequestBodyStr, requestBodyStr);
        return jsRunner.runScriptWithTransaction(filename);
    }

    @PostMapping("/clearCache")
    public Result<Void> clearCache() {
        return jsRunner.clearCache();
    }
}
