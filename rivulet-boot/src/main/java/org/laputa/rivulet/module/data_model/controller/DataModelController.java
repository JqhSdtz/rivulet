package org.laputa.rivulet.module.data_model.controller;

import org.laputa.rivulet.common.model.Pagination;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.mvc.annotation.RequestBodyParam;
import org.laputa.rivulet.common.script.JsRunner;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.service.DataModelService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    private JsRunner jsRunner;

    @PostMapping("/create")
    public Result<Void> createDataModel(@RequestBody @Validated RvPrototype rvPrototype) {
        return dataModelService.createDataModel(rvPrototype);
    }

    @PostMapping("/query")
    public Result<Page<RvPrototype>> queryDataModel(@RequestBodyParam RvPrototype payload, @RequestBodyParam Pagination pagination) {
        RvPrototype rvPrototype = payload == null ? new RvPrototype() : payload;
        return dataModelService.queryDataModel(rvPrototype, pagination.getPageable());
    }

    @GetMapping("/single/{id}")
    public Result<RvPrototype> queryOne(@PathVariable String id) {
        return dataModelService.queryOne(id);
    }

    @GetMapping("/ModelDetailSchema")
    public Result<Object> getModelDetailSchema() {
        return jsRunner.runScript("/src/schemas/ModelDetail/index.mjs");
    }

    @GetMapping("/DataModelIndexSchema")
    public Result<Object> getFormSchema() {
        return jsRunner.runScript("/src/schemas/DataModelIndex.mjs");
    }

    @PostMapping("/clearScriptCache")
    public Result<Void> clearScriptCache() {
        return jsRunner.clearCache();
    }
}
