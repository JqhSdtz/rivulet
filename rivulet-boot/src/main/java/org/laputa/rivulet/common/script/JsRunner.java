package org.laputa.rivulet.common.script;

import cn.hutool.crypto.SecureUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

/**
 * @author JQH
 * @since 下午 9:52 22/07/16
 */
@Component
public class JsRunner {
    private final GenericObjectPool<Context> contextPool;

    private final Cache<String, Source> sourceCache;

    public JsRunner() {
        this.contextPool = new GenericObjectPool<>(new JsContextFactory());
        this.sourceCache = CacheBuilder.newBuilder().build();
    }

    @SneakyThrows
    public Value run(String scriptCode) {
        String key = SecureUtil.md5(scriptCode);
        Source source = sourceCache.getIfPresent(key);
        if (source == null) {
            source = Source.newBuilder("js", scriptCode, key)
                    .cached(true)
                    .buildLiteral();
            sourceCache.put(key, source);
        }
        Context context = contextPool.borrowObject();
        Value value = context.eval(source);
        contextPool.returnObject(context);
        return value;
    }
}
