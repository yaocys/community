package com.example.community.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 异常处理通知
 * 只扫描带有Controller注解的Bean
 * @author yao 2022/11/28
 */
@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    @ExceptionHandler({Exception.class})
    public void handleException(Exception exception, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("服务器发生异常："+exception.getMessage());
        for(StackTraceElement stackTraceElement:exception.getStackTrace()){
            log.error(stackTraceElement.toString());
        }

        // 判断请求类型，是返回页面还是返回JSON数据
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            // 返回普通的字符串格式
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
        }else response.sendRedirect(request.getContextPath()+"/error");
    }
}
