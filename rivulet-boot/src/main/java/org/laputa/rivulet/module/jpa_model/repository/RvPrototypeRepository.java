package org.laputa.rivulet.module.jpa_model.repository;

import org.laputa.rivulet.module.jpa_model.entity.RvPrototype;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RvPrototypeRepository extends JpaRepository<RvPrototype, String> {
    @EntityGraph(attributePaths = {"properties"})
    @Query("SELECT p FROM RvPrototype p")
    List<RvPrototype> findAllWithProperties();
}
