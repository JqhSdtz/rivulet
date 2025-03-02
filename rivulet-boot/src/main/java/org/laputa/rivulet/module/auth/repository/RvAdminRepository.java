package org.laputa.rivulet.module.auth.repository;

import org.laputa.rivulet.module.auth.entity.RvAdmin;
import org.laputa.rivulet.module.auth.entity.dict.AdminType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author dell
 */
@Repository
public interface RvAdminRepository extends JpaRepository<RvAdmin, String> {
    /**
     * 根据用户名查找用户
     * @param adminName
     * @return
     */
    Optional<RvAdmin> findByAdminName(String adminName);

    List<RvAdmin> findByAdminType(AdminType adminType);
}
