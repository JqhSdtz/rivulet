package org.laputa.rivulet.module.dbms_model.repository;

import org.laputa.rivulet.module.dbms_model.entity.constraint.RvNotNull;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JQH
 * @since 下午 7:51 23/04/15
 */
public interface RvNotNullRepository extends JpaRepository<RvNotNull, String> {
}
