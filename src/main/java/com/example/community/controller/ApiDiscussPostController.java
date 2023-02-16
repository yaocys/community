package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.Event;
import com.example.community.entity.User;
import com.example.community.event.EventProducer;
import com.example.community.service.CommentService;
import com.example.community.service.DiscussPostService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.HostHolder;
import com.example.community.util.RedisKeyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/16 23:28
 */
@Api(tags = "前后端分离-帖子操作API")
@RestController
@RequestMapping("/post")
public class ApiDiscussPostController implements CommunityConstant {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private UserService userService;
    @Resource
    private CommentService commentService;
    @Resource
    private LikeService likeService;
    @Resource
    EventProducer eventProducer;
    @Resource
    RedisTemplate<String, Integer> redisTemplate;

    @ApiOperation(value = "新增（发布）帖子", notes = "详细说明", httpMethod = "POST")
    @PostMapping(path = "/add")
    public ApiResult<String> publish(String title, String content) {

        User user = hostHolder.getUser();
        if (user == null) return ApiResult.fail(403, "用户未登录");

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());

        try {
            discussPostService.addDiscussPost(discussPost);
        } catch (Exception e) {
            return ApiResult.fail(500, "服务器异常");
        }

        /*
        触发发帖事件
         */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

                /*
        触发将影响帖子分数的事件时，将对应的帖子ID缓存起来，后面定时处理
         */
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        return ApiResult.success("帖子发布成功");
    }
}
