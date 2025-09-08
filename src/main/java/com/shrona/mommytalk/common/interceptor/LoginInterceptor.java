package com.shrona.mommytalk.common.interceptor;

import com.shrona.mommytalk.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        // CORS preflight 요청(OPTIONS)은 JWT 검증 없이 통과
        if ("OPTIONS".equalsIgnoreCase(requestMethod)) {
            log.debug("OPTIONS 요청은 JWT 검증 없이 통과. URI: {}", requestURI);
            return true;
        }

        // Authorization 헤더에서 JWT 토큰 확인
        String authorizationHeader = request.getHeader(JwtUtils.AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authorizationHeader)) {
            log.debug("Authorization 헤더가 없습니다. URI: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (!authorizationHeader.startsWith(JwtUtils.BEARER_PREFIX)) {
            log.debug("Bearer 토큰이 아닙니다. URI: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            // JWT 토큰 추출 및 검증
            String token = jwtUtils.substringToken(authorizationHeader);

            if (!jwtUtils.checkValidJwtToken(token)) {
                log.debug("유효하지 않은 JWT 토큰입니다. URI: {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // JWT에서 사용자 정보 추출하여 request attribute에 저장
            Claims claims = jwtUtils.getBodyFromJwt(token);
            String userId = claims.getSubject();
            String userRole = claims.get(JwtUtils.AUTHORIZATION_KEY, String.class);

            // 컨트롤러에서 사용할 수 있도록 request attribute에 저장
            request.setAttribute("userId", Long.parseLong(userId));
            request.setAttribute("userRole", userRole);

            log.debug("JWT 인증 성공. UserId: {}, Role: {}, URI: {}", userId, userRole, requestURI);
            return true;

        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생. URI: {}, Error: {}", requestURI, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

}
