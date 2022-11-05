package org.laputa.rivulet.module.data_model.repository;

import org.laputa.rivulet.module.data_model.entity.RvForeignKey;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvForeignKeyRepository extends JpaRepository<RvForeignKey, String> {

}
