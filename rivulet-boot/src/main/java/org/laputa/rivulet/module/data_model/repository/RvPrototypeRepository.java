package org.laputa.rivulet.module.data_model.repository;

import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvPrototypeRepository extends JpaRepository<RvPrototype, String> {
    @Override
    List<RvPrototype> findAll();
}
