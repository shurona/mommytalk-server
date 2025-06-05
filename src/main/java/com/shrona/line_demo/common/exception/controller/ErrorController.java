package com.shrona.line_demo.common.exception.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @RequestMapping("/notFound")
    public String notFound() {
        return "forward:/admin";
    }

    @RequestMapping("/error/500")
    public String httpExceptionHandleMethod() {
        return "error/500";
    }

}
