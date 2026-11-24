package com.lab.emp.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.emp.common.Result;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 会话拦截器：受保护接口必须在 Session 中携带已登录员工编号。
 * <p>
 * 与指导书示例的差异：返回真实 HTTP 401 状态码 + 统一 JSON 结构，
 * 而不是 HTTP 200 里手写一段异常字符串，便于前端与接口工具统一判断。
 */
public class LoginInterceptor implements HandlerInterceptor {

    /** Session 中保存当前登录员工的键 */
    public static final String SESSION_EMP_ID = "loginEmpId";
    public static final String SESSION_EMP_NAME = "loginEmpName";

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SESSION_EMP_ID) != null) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.writeValueAsString(Result.unauthorized()));
        return false;
    }
}
