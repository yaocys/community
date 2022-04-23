package com.example.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yao 2022/4/12
 */
@Controller
@RequestMapping("/demo")
public class HelloDemo {
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello wrold!";
    }
}
