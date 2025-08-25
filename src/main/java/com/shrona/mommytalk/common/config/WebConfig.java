package com.shrona.mommytalk.common.config;

import com.shrona.mommytalk.common.filter.LineHookFilter;
import com.shrona.mommytalk.common.interceptor.LoginInterceptor;
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
        registry.addInterceptor(new LoginInterceptor())
            .order(1)
            .addPathPatterns("/**")
            .excludePathPatterns("/", "/admin", "/admin/", "/admin/v1/login", "/api/v1/admin",
                "/logout", "/css/**", "/*.ico", "/error",
                "/mommy-talk", "/shrona-test" // hook
            );

        // 채널 관련 요청 interceptor
//        registry.addInterceptor(new ChannelIdInterceptor())
//            .order(2)
//            .addPathPatterns("/admin/channels/**");
    }

}
