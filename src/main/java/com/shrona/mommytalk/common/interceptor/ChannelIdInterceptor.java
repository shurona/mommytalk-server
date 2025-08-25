package com.shrona.mommytalk.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * interceptor로 헤더 정보 공통 처리
 */
public class ChannelIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin/channels/")) {
            // /admin/channels/ 다음 숫자 뽑기
            String[] parts = uri.split("/");
            if (parts.length > 3) {
                try {
                    Long channelId = Long.parseLong(parts[3]);

                    request.setAttribute("channelId", channelId);
                } catch (NumberFormatException ignored) {
                    // 잘못된 channelId 형식이면 무시
                }
            }
        }
        return true;
    }
}
