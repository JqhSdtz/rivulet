package org.laputa.rivulet.module.app.model;

import lombok.Data;
import org.laputa.rivulet.common.state.AppState;
import org.laputa.rivulet.module.auth.entity.RvAdmin;

/**
 * @author JQH
 * @since 下午 9:52 22/03/30
 */
@Data
public class AppInitialData {
    private AppState appState;
    private RvAdmin currentAdmin;
}
