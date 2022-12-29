package com.example.community;

import com.example.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class  TestFilter{

    @Autowired
    SensitiveFilter sensitiveFilter;
    @Test
    public void tests(){

        String ss = "尽情嫖娼？？！开票测试过滤器";
        System.out.println(sensitiveFilter.filter(ss));
    }
}
