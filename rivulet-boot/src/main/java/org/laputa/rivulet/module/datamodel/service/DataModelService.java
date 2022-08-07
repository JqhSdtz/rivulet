package org.laputa.rivulet.module.datamodel.service;

import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.ddl.HibernateModelModifier;
import org.laputa.rivulet.ddl.LiquibaseDdlExecutor;
import org.laputa.rivulet.module.datamodel.entity.RvPrototype;
import org.laputa.rivulet.module.datamodel.repository.RvPrototypeRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JQH
 * @since 下午 9:47 22/06/26
 */
@Service
public class DataModelService {
    @Resource
    private RvPrototypeRepository rvPrototypeRepository;

    @Resource
    private LiquibaseDdlExecutor ddlExecutor;

    @Resource
    private HibernateModelModifier modelModifier;

    public Result<Void> createDataModel(RvPrototype rvPrototype) {
        rvPrototypeRepository.save(rvPrototype);
        ddlExecutor.doUpdate(ddlExecutor.addTable(rvPrototype, null));
        modelModifier.createModel(modelModifier.addTable(rvPrototype, null));
        return Result.succeed();
    }
}
