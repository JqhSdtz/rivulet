package org.laputa.rivulet.module.data_model.controller;

import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.service.DataModelService;
import org.laputa.rivulet.module.data_model.service.FormSchemaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JQH
 * @since 下午 9:38 22/06/26
 */
@RestController
@RequestMapping("/api/dataModel")
public class DataModelController {
    @Resource
    private DataModelService dataModelService;

    @Resource
    private FormSchemaService formSchemaService;

    @PostMapping
    public Result<Void> createDataModel(@RequestBody @Validated RvPrototype rvPrototype) {
        return dataModelService.createDataModel(rvPrototype);
    }

    @GetMapping
    public Result<List<RvPrototype>> queryDataModel(RvPrototype prototype) {
        return dataModelService.queryDataModel(prototype);
    }

    @GetMapping("/single/{id}")
    public Result<RvPrototype> queryOne(@PathVariable String id) {
        return dataModelService.queryOne(id);
    }

    @GetMapping("/form_schema")
    public Result<Object> getFormSchema() {
        return formSchemaService.getFormSchema();
    }
}
