package org.laputa.rivulet.module.data_model.controller;

import jakarta.annotation.Resource;
import org.laputa.rivulet.common.model.Pagination;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.mvc.annotation.RequestBodyParam;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.service.DataModelService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author JQH
 * @since 下午 9:38 22/06/26
 */
@RestController
@RequestMapping("/api/dataModel")
public class DataModelController {
    @Resource
    private DataModelService dataModelService;

    @PostMapping("/query")
    public Result<Page<RvPrototype>> queryDataModel(@RequestBodyParam RvPrototype payload, @RequestBodyParam Pagination pagination) {
        RvPrototype rvPrototype = payload == null ? new RvPrototype() : payload;
        return dataModelService.queryDataModel(rvPrototype, pagination.getPageable());
    }
}
