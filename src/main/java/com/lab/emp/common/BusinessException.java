package com.lab.emp.common;

/**
 * 业务异常：用于 Service 层主动抛出可预期的业务错误，
 * 由 GlobalExceptionHandler 统一转换为 Result，避免把堆栈暴露给前端。
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(500, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
