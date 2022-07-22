package org.laputa.rivulet.module.dict.repository;

import org.laputa.rivulet.module.dict.entity.RvDict;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author JQH
 * @since 下午 6:52 22/07/22
 */
public interface RvDictRepository extends JpaRepository<RvDict, String> {

}
