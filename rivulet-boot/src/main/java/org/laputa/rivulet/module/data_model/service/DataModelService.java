package org.laputa.rivulet.module.data_model.service;

import jakarta.annotation.Resource;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.ddl.LiquibaseDdlExecutor;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.repository.RvPrototypeRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public Result<Void> createDataModel(RvPrototype rvPrototype) {
        rvPrototype.setSyncFlag(false);
        rvPrototypeRepository.save(rvPrototype);
        ddlExecutor.doUpdate(ddlExecutor.addTable(rvPrototype, null));
        return Result.succeed();
    }

    public Result<Page<RvPrototype>> queryDataModel(RvPrototype prototype, Pageable pagination) {
        Example<RvPrototype> example = Example.of(prototype);
        Page<RvPrototype> list = rvPrototypeRepository.findAll(example, pagination);
        return Result.succeed(list);
    }

    public Result<RvPrototype> queryOne(String id) {
        Optional<RvPrototype> rvPrototypeOptional = rvPrototypeRepository.findById(id);
        if (rvPrototypeOptional.isEmpty()) {
            return Result.fail(RvPrototype.class, "NoEntityFound", "未找到ID为" + id + "的实体");
        }
        return Result.succeed(rvPrototypeOptional.get());
    }
}
