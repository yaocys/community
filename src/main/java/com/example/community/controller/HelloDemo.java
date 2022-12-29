package com.example.community.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yao 2022/4/12
 */
@Api(tags = "Hello示例API")
@Deprecated
@Controller
@RequestMapping("/demo")
public class HelloDemo {
    @ApiOperation("Hello World")
    @GetMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello wrold!";
    }
}
