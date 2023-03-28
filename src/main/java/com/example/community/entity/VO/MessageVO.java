package com.example.community.entity.VO;

import com.example.community.entity.Message;
import com.example.community.entity.User;
import lombok.Data;

/**
 * @author yaosunique@gmail.com
 * 2023/3/28 14:48
 */
@Data
public class MessageVO extends Message {
    private int letterCount;

    private int unreadCount;
    private User target;

    public MessageVO(Message message) {
        this.setId(message.getId());
        this.setFromId(message.getFromId());
        this.setToId(message.getToId());
        this.setConversationId(message.getConversationId());
        this.setContent(message.getContent());
        this.setStatus(message.getStatus());
        this.setCreateTime(message.getCreateTime());
    }
}
