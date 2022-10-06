package org.laputa.rivulet.module.datamodel.repository;

import org.laputa.rivulet.module.datamodel.entity.RvColumn;
import org.laputa.rivulet.module.datamodel.entity.RvPrototype;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvColumnRepository extends JpaRepository<RvColumn, String> {

}
