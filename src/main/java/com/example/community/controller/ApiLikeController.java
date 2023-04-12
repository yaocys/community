package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.Event;
import com.example.community.entity.User;
import com.example.community.entity.VO.LikeVO;
import com.example.community.event.EventProducer;
import com.example.community.service.LikeService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.HostHolder;
import com.example.community.util.RedisKeyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 21:40
 */
@Api(tags = "点赞操作API")
@RestController
public class ApiLikeController implements CommunityConstant {

    @Resource
    private LikeService likeService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "点赞", notes = "点两下取消")
    @PostMapping("/like")
    public ApiResult<LikeVO> like(int entityType, int entityId, int entityAuthorId, int postId) {
        User user = hostHolder.getUser();

        likeService.like(user.getId(), entityType, entityId, entityAuthorId);

        boolean likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        /*
        触发点赞事件，发送消息
         */
        if (likeStatus) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityAuthorId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        if (entityType == ENTITY_TYPE_POST) {
            /*
            将贴子ID添加到变更帖子列表，后面定时更新
             */
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        /*
         * 因为这里前端点赞的状态并不是后端的实时真实数据，所以这里其实不用返回任何数据
         */
        return ApiResult.success("点赞/取消点赞 成功");
    }
}
