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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author JQH
 * @since 下午 9:52 22/07/16
 */

@Component
public class JsRunner {
    @Resource
    private JsContextFactory jsContextFactory;

    private GenericObjectPool<Context> contextPool;

    private Cache<String, Source> sourceCache;

    @PostConstruct
    public void postConstruct() {
        this.contextPool = new GenericObjectPool<>(jsContextFactory);
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
