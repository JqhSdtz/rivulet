package org.laputa.rivulet.module.datamodel.repository;

import org.laputa.rivulet.module.datamodel.entity.RvPrototype;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvPrototypeRepository extends JpaRepository<RvPrototype, String> {

}
