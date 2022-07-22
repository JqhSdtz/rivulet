package org.laputa.rivulet.module.datamodel.service;

import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.datamodel.entity.RvTable;
import org.laputa.rivulet.module.datamodel.repository.RvTableRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JQH
 * @since 下午 9:47 22/06/26
 */
@Service
public class DataModelService {
    @Resource
    private RvTableRepository rvTableRepository;

    public Result<Void> createDataModel(RvTable rvTable) {
        rvTableRepository.save(rvTable);
        return Result.succeed();
    }
}
