package org.laputa.rivulet.module.data_model.controller;

import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.data_model.service.BuiltInDataModelService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * @author JQH
 * @since 下午 10:20 22/11/03
 */
@RestController
@RequestMapping("/api/builtInDataModel")
public class BuiltInDataModelController {
    @Resource
    private BuiltInDataModelService builtInDataModelService;

    @PostMapping("/confirmUpdateSql")
    public Result<String> confirmStructureUpdateSql(@RequestBody Map<String, String> params) {
        return builtInDataModelService.confirmUpdateStructureSql(params.get("confirmKey"));
    }
}
