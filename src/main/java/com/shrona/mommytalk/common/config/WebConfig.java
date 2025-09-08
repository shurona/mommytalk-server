package com.shrona.mommytalk.common.config;

import com.shrona.mommytalk.common.filter.LineHookFilter;
import com.shrona.mommytalk.common.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // interceptor
    private final LoginInterceptor loginInterceptor;

    private final Environment environment;


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Body를 복사하는 Filter Bean(현재는 사용하지 않음)
     */
    public FilterRegistrationBean<LineHookFilter> myFilter() {
        FilterRegistrationBean<LineHookFilter> registrationBean = new FilterRegistrationBean<>(
            new LineHookFilter(environment));
        registrationBean.setOrder(1); // 필터 순서 지정
        registrationBean.addUrlPatterns("/"); // URL 패턴 지정
        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로그인 interceptor
        registry.addInterceptor(loginInterceptor)
            .order(1)
            .addPathPatterns("/**")
            .excludePathPatterns("/", "/admin", "/admin/", "/admin/v1/login", "/api/v1/admin",
                "/api/admin/v1/auth/login", // admin login
                "/logout", "/css/**", "/*.ico", "/error", // static files
                "/mommy-talk", "/shrona-test" // hook
            );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:5173",   // Vite 기본 포트
                "http://localhost:5174"   // Vite 대체 포트
            )
            .allowedMethods("GET", "POST", "PATCH", "DELETE", "PUT")
            .allowedHeaders(
                "Authorization",           // JWT 토큰용
                "Content-Type",           // JSON 요청용
                "X-Requested-With",       // AJAX 요청용
                "Accept"                  // 응답 타입용
            )
            .allowCredentials(true);          // 인증 정보 포함 허용
    }

}
