package org.laputa.rivulet.module.dbms_model.repository;

import org.laputa.rivulet.module.dbms_model.entity.RvColumn;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvColumnRepository extends JpaRepository<RvColumn, String> {

}
