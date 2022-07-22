package org.laputa.rivulet;

import org.laputa.rivulet.module.auth.entity.RvUser;
import org.laputa.rivulet.module.auth.repository.RvUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author JQH
 * @since 下午 8:54 22/02/08
 */
@Component
@Order(2)
public class ForTest implements ApplicationRunner {

    private final RvUserRepository rvUserRepository;

    public ForTest(RvUserRepository rvUserRepository) {
        this.rvUserRepository = rvUserRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
//        test();
    }

    public void test() {
        RvUser user1 = new RvUser();
        user1.setUsername("user1");
        user1.setPassword("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        rvUserRepository.save(user1);
        RvUser user2 = new RvUser();
        user2.setUsername("user2");
        user2.setPassword("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        rvUserRepository.save(user2);
    }
}
