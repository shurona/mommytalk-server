package com.shrona.line_demo.common.config;

import com.shrona.line_demo.common.filter.LineHookFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class FilterConfig implements WebMvcConfigurer {

    private final Environment environment;

    //    @Bean
    public FilterRegistrationBean<LineHookFilter> myFilter() {
        FilterRegistrationBean<LineHookFilter> registrationBean = new FilterRegistrationBean<>(
            new LineHookFilter(environment));
        registrationBean.setOrder(1); // 필터 순서 지정
        registrationBean.addUrlPatterns("/"); // URL 패턴 지정
        return registrationBean;
    }

}
