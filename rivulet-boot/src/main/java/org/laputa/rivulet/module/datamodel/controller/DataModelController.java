package org.laputa.rivulet.module.datamodel.controller;

import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.datamodel.entity.RvPrototype;
import org.laputa.rivulet.module.datamodel.service.DataModelService;
import org.laputa.rivulet.module.datamodel.service.FormSchemaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JQH
 * @since 下午 9:38 22/06/26
 */
@RestController
@RequestMapping("/api/data_model")
public class DataModelController {
    @Resource
    private DataModelService dataModelService;

    @Resource
    private FormSchemaService formSchemaService;

    @PostMapping
    public Result<Void> createDataModel(@RequestBody @Validated RvPrototype rvPrototype) {
        return dataModelService.createDataModel(rvPrototype);
    }

    @GetMapping("/form_schema")
    public Result<Object> getFormSchema() {
        return formSchemaService.getFormSchema();
    }
}
