package org.laputa.rivulet.module.app.repository;

import org.laputa.rivulet.module.app.entity.AppRuntimeLog;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppRuntimeLogRepository extends JpaRepository<AppRuntimeLog, String> {
    List<AppRuntimeLog> findByOrderByLogTimeDesc(Limit limit);
}
