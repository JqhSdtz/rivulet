package org.laputa.rivulet.module.auth.entity;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.laputa.rivulet.common.util.DatabaseUtil;
import org.laputa.rivulet.common.util.SpringBeanUtil;
import org.laputa.rivulet.module.app.service.AppInitService;
import org.laputa.rivulet.module.auth.entity.dict.AdminType;


public class RvAdminIdentityGeneratorImpl implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        RvAdmin admin = (RvAdmin) object;
        AppInitService appInitService = SpringBeanUtil.getBean(AppInitService.class);
        if (admin.getAdminType() == AdminType.INITIAL_ADMIN) {
            return appInitService.getInitialAdminId();
        } else {
            return DatabaseUtil.generateId();
        }
    }
}
