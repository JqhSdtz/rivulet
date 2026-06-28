package org.laputa.rivulet.common.state;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppState {
    /**
     * 应用是否初始化的标志
     */
    private final EventBus.State<Boolean> initAdminCreated = new EventBus.State<>("InitAdminCreated", false);

    /**
     * 内部数据模型是否同步完成
     */
    private final EventBus.State<Boolean> allLoadedDataModelSynced = new EventBus.State<>("AllLoadedDataModelSynced", false);
    /**
     * 数据模型发生更新
     */
    private final EventBus.State<Boolean> dataModelChanged = new EventBus.State<>("dataModelChanged", false);
}
