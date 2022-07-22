package org.laputa.rivulet.common.script;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.graalvm.polyglot.Context;

/**
 * @author JQH
 * @since 下午 9:19 22/07/16
 */
public class JsContextFactory extends BasePooledObjectFactory<Context> {
    @Override
    public Context create() {
        Context context = Context.newBuilder("js")
                .option("engine.WarnInterpreterOnly", "false")
                .build();
        return context;
    }

    @Override
    public PooledObject<Context> wrap(Context context) {
        return new DefaultPooledObject<>(context);
    }
}
