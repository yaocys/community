package com.example.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yao 2022/11/28
 */
@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     * 对service包下所有类，所有参数返回值的所有方法
     */
    @Before("execution(* com.example.community.service.*.*(..))")
    public void before(JoinPoint join){
        /*
        获取request对象进而获取用户的IP
         */
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        String IP = request.getRemoteHost();

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 获取执行的方法以其返回值
        String target = join.getSignature().getDeclaringTypeName()+join.getSignature().getName();

        logger.info(String.format("用户[%s]，在[%s]，访问了[%s]",IP,now,target));
    }
}
