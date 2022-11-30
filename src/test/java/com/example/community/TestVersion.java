package com.example.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.SpringVersion;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yao 2022/11/30
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TestVersion {

    String springVersion = SpringVersion.getVersion();
    String springBootVersion = SpringBootVersion.getVersion();

    @Test
    public void getVersion(){
        System.out.println(springVersion);
        System.out.println(springBootVersion);
    }
}
