package org.laputa.rivulet.module.data_model.repository;

import org.laputa.rivulet.module.data_model.entity.constraint.RvUnique;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvUniqueRepository extends JpaRepository<RvUnique, String> {

}
