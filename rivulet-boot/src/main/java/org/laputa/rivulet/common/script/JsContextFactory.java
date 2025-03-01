package org.laputa.rivulet.common.script;

import jakarta.annotation.Resource;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 9:19 22/07/16
 */
@Component
public class JsContextFactory extends BasePooledObjectFactory<Context> {
    @Resource
    private JavaNative javaNative;

    @Override
    public Context create() {
        Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .build();
        Value bindings = context.getBindings("js");
        bindings.putMember("javaNative", javaNative);
        return context;
    }

    @Override
    public PooledObject<Context> wrap(Context context) {
        return new DefaultPooledObject<>(context);
    }
}
