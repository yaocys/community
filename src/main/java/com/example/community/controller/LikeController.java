package com.example.community.controller;

import com.example.community.entity.Event;
import com.example.community.entity.User;
import com.example.community.event.EventProducer;
import com.example.community.service.LikeService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import com.example.community.util.RedisKeyUtil;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yao 2022/6/16
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞接口
     * @param entityType 点赞的目标实体类型
     * @param entityId 点赞目标实体类型的ID
     */
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityAuthorId,int postId){
        // TODO 这里后面会用权限管理框架统一处理
        User user = hostHolder.getUser();

        likeService.like(user.getId(), entityType,entityId,entityAuthorId);

        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        int likeStatus= likeService.findEntityLikeStatus(user.getId(), entityType,entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        /*
        触发点赞事件，发送消息
         */
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityAuthorId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        if(entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
