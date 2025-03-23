package org.laputa.rivulet.module.dbms_model.controller;

import jakarta.annotation.Resource;
import org.laputa.rivulet.module.dbms_model.service.BuiltInDataModelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author JQH
 * @since 下午 5:53 22/10/23
 */
@Controller
@RequestMapping("/page/dataModel")
public class DataModelPageController {
    @Resource
    private BuiltInDataModelService builtInDataModelService;

    @RequestMapping("builtInModifyConfirm")
    public String builtInModifyConfirm(Model model) {
        model.addAttribute("sql", builtInDataModelService.getCurrentStructureUpdateSql());
        return "data_model/dataModelModifyConfirmPage";
    }
}
