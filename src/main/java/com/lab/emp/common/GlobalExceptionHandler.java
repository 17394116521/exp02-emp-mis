package com.lab.emp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把不同来源的异常统一转换成 code/message/data 结构，
 * 前端只需按 code 判断，不必解析 HTML 错误页或堆栈信息。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** JSR-303 参数校验失败：取第一条字段错误提示返回 400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数不合法" : fieldError.getDefaultMessage();
        log.warn("参数校验未通过：{}", message);
        return Result.fail(400, message);
    }

    /** 请求体不是合法 JSON，或字段类型无法转换 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.fail(400, "请求体格式错误，请检查 JSON 结构与字段类型");
    }

    /** 业务层主动抛出的可预期错误 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 兜底：记录日志，向前端返回统一提示，避免泄露堆栈 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("未预期的系统异常", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
