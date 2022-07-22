package org.laputa.rivulet.module.auth.repository;

import org.laputa.rivulet.module.auth.entity.RvUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author dell
 */
@Repository
public interface RvUserRepository extends JpaRepository<RvUser, String> {
    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    Optional<RvUser> findByUsername(String username);
}
