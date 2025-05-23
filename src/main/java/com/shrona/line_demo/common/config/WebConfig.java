package com.shrona.line_demo.common.config;

import com.shrona.line_demo.common.filter.LineHookFilter;
import com.shrona.line_demo.common.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Environment environment;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //    @Bean
    public FilterRegistrationBean<LineHookFilter> myFilter() {
        FilterRegistrationBean<LineHookFilter> registrationBean = new FilterRegistrationBean<>(
            new LineHookFilter(environment));
        registrationBean.setOrder(1); // 필터 순서 지정
        registrationBean.addUrlPatterns("/"); // URL 패턴 지정
        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
            .order(1)
            .addPathPatterns("/**")
            .excludePathPatterns("/", "/admin", "/admin/v1/login",
                "/logout", "/css/**", "/*.ico", "/error");
    }
}
