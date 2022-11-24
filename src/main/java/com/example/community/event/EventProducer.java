package com.example.community.event;

import com.alibaba.fastjson.JSONObject;
import com.example.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author yao 2022/11/24
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件（向消息队列发消息）
     * @param event 事件对象
     */
    public void fireEvent(Event event){
        /*
        将事件发送到指定主题
         */
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
