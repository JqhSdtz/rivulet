package org.laputa.rivulet;

import org.laputa.rivulet.ddl.LiquibaseDdlExecutor;
import org.laputa.rivulet.module.auth.entity.RvUser;
import org.laputa.rivulet.module.auth.repository.RvUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author JQH
 * @since 下午 8:54 22/02/08
 */
@Component
@Order(2)
public class ForTest implements ApplicationRunner {
    @Resource
    private LiquibaseDdlExecutor ddlExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
//        String type = ddlExecutor.convertDataTypeToSqlType("java.sql.Types.VARCHAR(77)", false);
//        System.out.println(type);
    }

}
