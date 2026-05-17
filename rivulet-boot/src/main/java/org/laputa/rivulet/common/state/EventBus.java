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
public class EventBus {
    /**
     * 状态变更回调的映射关系
     */
    private static final Map<State<?>, List<Consumer<State<?>>>> callbackMap = new HashMap<>();

    @Getter
    public static class State<T> {
        String name;
        T previousValue;
        T currentValue;

        State(String name, T value) {
            this.name = name;
            this.currentValue = value;
        }

        public void setValue(T value) {
            if (currentValue.equals(value)) {
                return;
            }
            this.previousValue = currentValue;
            this.currentValue = value;
            List<Consumer<State<?>>> callbackList = callbackMap.get(this);
            if (callbackList != null) {
                callbackList.forEach(callback -> {
                    callback.accept(this);
                });
            }
        }
    }

    public static void registerStateChangeCallback(State<?> state, Consumer<State<?>> consumer) {
        List<Consumer<State<?>>> callbackList = callbackMap.computeIfAbsent(state, k -> new ArrayList<>());
        callbackList.add(consumer);
    }

}
