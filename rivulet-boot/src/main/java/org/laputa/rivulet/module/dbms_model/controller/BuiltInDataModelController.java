package org.laputa.rivulet.module.dbms_model.controller;

import jakarta.annotation.Resource;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.dbms_model.service.BuiltInDataModelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
