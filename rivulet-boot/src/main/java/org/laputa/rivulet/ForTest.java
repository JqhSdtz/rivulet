package org.laputa.rivulet;

import lombok.SneakyThrows;
import org.laputa.rivulet.ddl.LiquibaseDdlExecutor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {

    }

}
