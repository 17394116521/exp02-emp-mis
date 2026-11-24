package com.lab.emp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 请求日志切面：在 Controller 层统一记录入口、参数、耗时与出口状态。
 * <p>
 * 相比指导书示例的差异点：
 * 1. 每次请求生成 traceId 写入 MDC，日志行可串联同一请求的全链路；
 * 2. 口令类参数脱敏输出，避免日志泄露登录凭据；
 * 3. 按耗时分级，超过 500ms 记为 WARN，便于定位慢接口；
 * 4. 异常时记录异常类型与信息后原样抛出，不影响全局异常处理器。
 */
@Aspect
@Component
public class RequestTraceAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestTraceAspect.class);

    /** 慢请求阈值（毫秒） */
    private static final long SLOW_THRESHOLD_MS = 500L;

    /** 切点：只拦截表现层，Service 与 Mapper 不重复打印 */
    @Pointcut("execution(public * com.lab.emp.controller..*.*(..))")
    public void controllerLayer() {
    }

    @Around("controllerLayer()")
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        HttpServletResponse response = attributes == null ? null : attributes.getResponse();

        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        MDC.put("traceId", traceId);

        String target = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "#" + joinPoint.getSignature().getName();
        String method = request == null ? "-" : request.getMethod();
        String uri = request == null ? "-" : request.getRequestURI();
        long start = System.currentTimeMillis();

        log.info("{} {} -> {}，参数={}", method, uri, target, safeParams(request));
        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;
            int status = response == null ? 200 : response.getStatus();
            if (cost >= SLOW_THRESHOLD_MS) {
                log.warn("{} {} <- status={}，耗时 {}ms，超过慢请求阈值 {}", method, uri, status, cost, SLOW_THRESHOLD_MS);
            } else {
                log.info("{} {} <- status={}，耗时 {}ms", method, uri, status, cost);
            }
            return result;
        } catch (Throwable t) {
            long cost = System.currentTimeMillis() - start;
            log.error("{} {} <- 抛出 {}: {}，耗时 {}ms", method, uri, t.getClass().getSimpleName(), t.getMessage(), cost);
            throw t;
        } finally {
            MDC.remove("traceId");
        }
    }

    /** 查询参数脱敏：口令类字段一律输出 *** */
    private Map<String, String> safeParams(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        if (request == null || request.getParameterMap().isEmpty()) {
            return params;
        }
        request.getParameterMap().forEach((key, values) -> {
            String lower = key.toLowerCase();
            boolean sensitive = lower.contains("password") || lower.contains("pwd") || lower.contains("token");
            params.put(key, sensitive ? "***" : (values.length == 0 ? "" : values[0]));
        });
        return params;
    }
}
