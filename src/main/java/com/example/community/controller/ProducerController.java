package com.example.community.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试向Kafka发消息
 *
 * @author yao 2022/11/24
 */
@Api(tags = "测试向Kafka发消息")
@Deprecated
@RestController
public class ProducerController {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @ApiOperation("向Kafka发送消息")
    @PostMapping("/sendMsg")
    public String send(String msg) {

        kafkaTemplate.send("test", msg);

        return "ok";
    }
}
