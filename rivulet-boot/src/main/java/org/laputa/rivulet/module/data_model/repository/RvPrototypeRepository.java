package org.laputa.rivulet.module.data_model.repository;

import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

/**
 * @author JQH
 * @since 下午 9:45 22/06/26
 */
public interface RvPrototypeRepository extends JpaRepository<RvPrototype, String> {
    @Override
    @QueryHints({@QueryHint(name = AvailableHints.HINT_CACHEABLE, value = "true"),
            @QueryHint(name = AvailableHints.HINT_CACHE_REGION, value = "defaultCache")
    })
    List<RvPrototype> findAll();
}
