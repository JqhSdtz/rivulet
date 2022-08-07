package org.laputa.rivulet.module.datamodel.service;

import cn.hutool.core.io.resource.ResourceUtil;
import org.graalvm.polyglot.Value;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.script.JsRunner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JQH
 * @since 下午 12:22 22/07/03
 */

@Service
public class FormSchemaService {
    @Resource
    private JsRunner jsRunner;
    public Result<Object> getFormSchema() {
        String scriptText = ResourceUtil.readUtf8Str("file:D:\\WORKSPACE\\laputa\\rivulet\\rivulet-boot\\src\\main\\resources\\static\\scripts\\ModelDetail.js");
        Value result = jsRunner.run(scriptText);
        return Result.succeed(result.asString());
    }
}
