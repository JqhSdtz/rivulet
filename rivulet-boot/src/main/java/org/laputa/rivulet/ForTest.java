package org.laputa.rivulet;

import org.hibernate.SessionFactory;
import org.laputa.rivulet.ddl.LiquibaseDdlExecutor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author JQH
 * @since 下午 8:54 22/02/08
 */
@Component
@Order(1)
public class ForTest implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        LiquibaseDdlExecutor ddlExecutor = new LiquibaseDdlExecutor();
    }
}
