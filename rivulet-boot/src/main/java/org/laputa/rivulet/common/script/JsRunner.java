package org.laputa.rivulet.common.script;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.app.property.GitProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;

/**
 * @author JQH
 * @since 下午 9:52 22/07/16
 */

@Component
public class JsRunner {
    private static final String NOT_RUNNABLE = "NotRunnable";
    @Resource
    private JsContextFactory jsContextFactory;
    @Resource
    private GitProperty gitProperty;

    private GenericObjectPool<Context> contextPool;

    private Cache<String, Source> sourceCache;

    @PostConstruct
    public void postConstruct() {
        this.contextPool = new GenericObjectPool<>(jsContextFactory);
        this.sourceCache = CacheBuilder.newBuilder().build();
    }

    private Value doRun(String scriptPath) throws Exception {
        Source source = sourceCache.getIfPresent(scriptPath);
        if (source == null) {
            source = Source.newBuilder("js", new File(gitProperty.getLocalDir() + scriptPath))
                    .mimeType("application/javascript+module")
                    .cached(true)
                    .build();
            sourceCache.put(scriptPath, source);
        }
        Context context = contextPool.borrowObject();
        Value value;
        try {
            value = context.eval(source);
            contextPool.returnObject(context);
            if (!value.canInvokeMember("run")) {
                return context.asValue(NOT_RUNNABLE);
            } else {
                return value.invokeMember("run");
            }
        } catch (PolyglotException exception) {
            exception.printStackTrace();
            sourceCache.invalidate(scriptPath);
            return null;
        }
    }

    @SneakyThrows
    public Result<Object> runScript(String scriptPath) {
        Value result = doRun(scriptPath);
        if (result == null) {
            return Result.fail(Object.class, "JavaScriptRunFailed", "脚本程序运行异常");
        } else if (!result.isString()) {
            return Result.fail(Object.class, "NonStringResult", "无法解析非字符串的返回结果");
        }else if (NOT_RUNNABLE.equals(result.asString())) {
            return Result.fail(Object.class, NOT_RUNNABLE, "脚本未设置运行接口");
        }
        return Result.succeed(result.asString());
    }

    public Result<Void> clearCache() {
        sourceCache.invalidateAll();
        contextPool.clear();
        return Result.succeedWithMessage("清除脚本缓存成功");
    }
}
