package org.laputa.rivulet.common.model;

import lombok.Getter;
import lombok.Setter;
import org.laputa.rivulet.common.exception.RvException;

/**
 * @author JQH
 * @since 下午 6:22 22/04/03
 */
@Setter
@Getter
public class Result<ResultType> {
    public static final String UNEXPECTED_ERROR = "UnexpectedError";
    public static final Result<Void> SUCCESS = new Result<>(true);
    public static final Result<Void> FAIL = new Result<>(false);

    private boolean successful;
    private ResultType payload = null;
    private String returnMessage;
    private String errorCode;
    private String errorMessage;
    private Exception rawException = null;

    /**
     * 创建一个空白的成功或失败的对象
     *
     * @param successful
     */
    private Result(boolean successful) {
        this.successful = successful;
    }

    public static Result<Void> empty(boolean success) {
        return success ? SUCCESS : FAIL;
    }

    public static Result<Void> succeed() {
        return empty(true);
    }

    public static <T> Result<T> succeed(T payload) {
        return succeed(payload, null);
    }

    public static <T> Result<T> succeedWithMessage(String returnMessage) {
        return succeed(null, returnMessage);
    }

    public static <T> Result<T> succeed(T payload, String returnMessage) {
        Result result = new Result(true);
        result.setPayload(payload);
        result.setReturnMessage(returnMessage);
        return result;
    }

    public static <T> Result<T> fail(Class<T> clazz, String errorCode, String errorMessage, T payload) {
        Result result = new Result<>(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        result.setPayload(payload);
        return result;
    }

    public static <T> Result<T> fail(Class<T> clazz, String errorCode, String errorMessage) {
        return fail(clazz, errorCode, errorMessage, null);
    }

    public static Result<Void> fail(String errorCode, String errorMessage) {
        return fail(Void.class, errorCode, errorMessage);
    }

    /**
     * 用于改变Result结果的内容类，若是成功对象，则将payload强制转换到指定类，
     * 若是失败对象，则直接创建一个errorCode和errorMessage相同的指定类的Result对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Result<T> ofClass(Class<T> clazz) {
        if (this.successful) {
            return succeed((T) this.payload);
        } else {
            return fail(clazz, this.errorCode, this.errorMessage, (T) this.payload);
        }
    }

    /**
     * 获取一个以当前Result对象为负载的RvException对象
     *
     * @return
     */
    public RvException toException() {
        return new RvException(this);
    }

    public Exception toRawException() {
        return rawException == null ? toException() : rawException;
    }

}
