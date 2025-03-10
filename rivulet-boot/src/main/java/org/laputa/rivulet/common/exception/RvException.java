package org.laputa.rivulet.common.exception;

import lombok.Getter;
import org.laputa.rivulet.common.model.Result;

/**
 * @author JQH
 * @since 下午 3:16 22/04/08
 */
@Getter
public class RvException extends RuntimeException {

    private final Result<?> failResult;

    public RvException(Result<?> failResult) {
        this.failResult = failResult;
    }

    /**
     * 禁止获取异常堆栈信息，降低开销
     *
     * @return
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
