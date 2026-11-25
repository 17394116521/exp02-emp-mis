package com.lab.emp.common;

/**
 * 统一返回格式：code / message / data。
 * <p>
 * 错误码约定：200 成功；400 参数或请求体不合法；401 未登录或口令错误；
 * 500 未预期的系统异常。业务层需要表达“资源不存在”时可传 404。
 * 前端只需按 code 分支处理，HTTP 状态码用于网关与监控层（例如拦截器返回真实 401）。
 */
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功并携带数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功且无返回数据 */
    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null);
    }

    /** 业务失败，默认 500 */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /** 指定错误码失败，例如 400 参数错误 */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /** 未登录，配合登录拦截器使用 */
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未登录或登录已过期", null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
