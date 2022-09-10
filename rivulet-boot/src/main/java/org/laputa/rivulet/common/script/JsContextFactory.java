package org.laputa.rivulet.common.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

/**
 * @author JQH
 * @since 下午 9:19 22/07/16
 */
@Component
public class JsContextFactory extends BasePooledObjectFactory<Context> {
    @Resource
    private JsGlobal jsGlobal;

    @Override
    public Context create() {
        Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();
        Value bindings = context.getBindings("js");
        bindings.putMember("jsGlobal", jsGlobal);
        return context;
    }

    @Override
    public PooledObject<Context> wrap(Context context) {
        return new DefaultPooledObject<>(context);
    }
}
