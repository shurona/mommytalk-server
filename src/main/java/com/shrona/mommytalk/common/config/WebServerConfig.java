package com.shrona.mommytalk.common.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class WebServerConfig implements
    WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    /**
     * 없는 페이지의 경우 admin 페이지로 redirect
     */
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/notFound"));
    }
}
