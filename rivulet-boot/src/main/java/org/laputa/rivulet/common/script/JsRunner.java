package org.laputa.rivulet.common.script;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.laputa.rivulet.common.exception.RvException;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.module.app.property.GitProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.NoSuchFileException;

/**
 * @author JQH
 * @since 下午 9:52 22/07/16
 */

@Component
public class JsRunner {
    private static final String NOT_RUNNABLE = "NotRunnable";
    private static final String NO_SUCH_FILE = "NoSuchFile";
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
        if (scriptPath == null || scriptPath.isEmpty()) {
            throw new RvException(Result.fail("EmptyScriptPath", "脚本路径为空"));
        }
        String[] parts = scriptPath.split("@");
        String filePath = parts.length > 1 ? parts[0] : scriptPath;
        String methodName = parts.length > 1 ? parts[1] : "run";
        Source source = sourceCache.getIfPresent(scriptPath);
        Context context = contextPool.borrowObject();
        Value value;
        if (source == null) {
            try {
                source = Source.newBuilder("js", new File(gitProperty.getLocalDir() + filePath))
                        .mimeType("application/javascript+module")
                        .cached(true)
                        .build();
            } catch (NoSuchFileException exception) {
                return context.asValue(NO_SUCH_FILE);
            }
            sourceCache.put(filePath, source);
        }
        try {
            value = context.eval(source);
            contextPool.returnObject(context);
            if (!value.canInvokeMember(methodName)) {
                return context.asValue(NOT_RUNNABLE);
            } else {
                return value.invokeMember(methodName);
            }
        } catch (PolyglotException exception) {
            exception.printStackTrace();
            sourceCache.invalidate(filePath);
            return null;
        }
    }

    @SneakyThrows
    public Result<?> runScript(String scriptPath) {
        Value value = doRun(scriptPath);
        if (value == null) {
            return Result.fail("JavaScriptRunFailed", "脚本程序运行异常");
        } else if (value.isString()) {
            if (NO_SUCH_FILE.equals(value.asString())) {
                return Result.fail(NO_SUCH_FILE, "未找到脚本文件");
            } else if (NOT_RUNNABLE.equals(value.asString())) {
                return Result.fail(NOT_RUNNABLE, "脚本未设置运行接口");
            } else {
                return Result.succeed(value.asString());
            }
        } else if (value.isBoolean()) {
            return value.asBoolean() ? Result.SUCCESS : Result.FAIL;
        } else if (value.isHostObject()) {
            Object result = value.asHostObject();
            if (result instanceof Result) {
                return (Result<?>) result;
            } else {
                return Result.fail("NonResultJavaObject", "非Result类型的Java对象结果");
            }
        } else {
            return Result.fail("InvalidResultType", "非法的返回值类型");
        }
    }

    @Transactional
    public Result<?> runScriptWithTransaction(String scriptPath) {
        return runScript(scriptPath);
    }

    public Result<Void> clearCache() {
        sourceCache.invalidateAll();
        contextPool.clear();
        return Result.succeedWithMessage("清除脚本缓存成功");
    }
}
