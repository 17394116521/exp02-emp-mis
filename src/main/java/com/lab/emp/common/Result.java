package com.lab.emp.common;

/**
 * 统一返回格式：code / message / data。
 * <p>
 * 约定：200 成功，500 业务或系统失败，401 未登录。
 * 前端只需判断 code，不必关心 HTTP 状态码细节。
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
