package org.laputa.rivulet.common.state;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author JQH
 * @since 下午 8:06 22/04/06
 */
@Component
public class AppState {
    /**
     * 状态变更回调的映射关系
     */
    private final Map<String, List<Consumer<State<?>>>> callbackMap = new HashMap<>();

    /**
     * 应用是否初始化的标志
     */
    private final State<Boolean> appInitialized = new State<>("appInitialized", false);
    /**
     * 内部数据模型是否同步完成
     */
    private final State<Boolean> builtInDataModelSynced = new State<>("builtInDataModelSynced", false);
    @Getter
    public class State<T> {
        String name;
        T previousValue;
        T currentValue;

        State(String name, T value) {
            this.name = name;
            this.currentValue = value;
        }
        void setValue(T value) {
            if (currentValue.equals(value)) {
                return;
            }
            this.previousValue = currentValue;
            this.currentValue = value;
            List<Consumer<State<?>>> callbackList = callbackMap.get(name);
            if (callbackList != null) {
                callbackList.forEach(callback -> {
                    callback.accept(this);
                });
            }
        }
    }
    public void setAppInitialized(Boolean appInitialized) {
        this.appInitialized.setValue(appInitialized);
    }
    public void setBuiltInDataModelSynced(Boolean builtInDataModelSynced) {
        this.builtInDataModelSynced.setValue(builtInDataModelSynced);
    }
    public Boolean isAppInitialized() {
        return appInitialized.currentValue;
    }
    public Boolean isBuiltInDataModelSynced() {
        return builtInDataModelSynced.currentValue;
    }
    public void registerStateChangeCallback(String name, Consumer<State<?>> consumer) {
        List<Consumer<State<?>>> callbackList = callbackMap.computeIfAbsent(name, k -> new ArrayList<>());
        callbackList.add(consumer);
    }

}
