package com.example.community;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author yaosunique@gmail.com
 * 2023/7/28 17:31
 */
@SpringBootTest
@Slf4j
public class LoggerTest {
    @Test
    public void log(){
        String name = "张三";
        int age = 24;
        log.info("用户：{} {}","李四",age);
        log.error("这是一个错误");
    }
}
