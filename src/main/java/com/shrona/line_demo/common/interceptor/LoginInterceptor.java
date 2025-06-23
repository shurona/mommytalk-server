package com.shrona.line_demo.common.interceptor;

import com.shrona.line_demo.common.utils.StaticVariable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(StaticVariable.LOGIN_USER) == null) {
            // 미인증 사용자
            response.sendRedirect("/admin?redirectURL=" + requestURI);
            return false;
        }

        return true;

    }

}
