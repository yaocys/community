package com.example.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试向Kafka发消息
 * @author yao 2022/11/24
 */
@Deprecated
@RestController
public class ProducerController {
    @Autowired
    KafkaTemplate<String ,String> kafkaTemplate;

    @RequestMapping("/sendMsg")
    public String send(String msg){

        kafkaTemplate.send("test",msg);

        return "ok";
    }
}
