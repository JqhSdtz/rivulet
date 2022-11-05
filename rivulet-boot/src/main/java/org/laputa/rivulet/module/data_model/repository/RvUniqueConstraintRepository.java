package org.laputa.rivulet.module.data_model.repository;

import org.laputa.rivulet.module.data_model.entity.RvUniqueConstraint;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvUniqueConstraintRepository extends JpaRepository<RvUniqueConstraint, String> {

}
