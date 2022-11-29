package com.example.community.event;

import com.alibaba.fastjson.JSONObject;
import com.example.community.entity.Event;
import com.example.community.entity.Message;
import com.example.community.service.MessageService;
import com.example.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yao 2022/11/24
 */
@Component
public class EventConsumer implements CommunityConstant {
    
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            logger.error("消息格式错误！");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        // 这里存的不再是会话的ID，而是主题，复用同一张表
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if(!event.getData().isEmpty())
            content.putAll(event.getData());

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}
