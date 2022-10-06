package org.laputa.rivulet.module.datamodel.repository;

import org.laputa.rivulet.module.datamodel.entity.RvColumn;
import org.laputa.rivulet.module.datamodel.entity.RvIndex;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvIndexRepository extends JpaRepository<RvIndex, String> {

}
