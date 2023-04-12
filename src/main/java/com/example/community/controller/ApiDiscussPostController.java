package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.Event;
import com.example.community.entity.User;
import com.example.community.event.EventProducer;
import com.example.community.service.DiscussPostService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.HostHolder;
import com.example.community.util.RedisKeyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @ApiOperation("帖子详情")
    @GetMapping("/detail/{discussPostId}")
    public ApiResult<Map<String, Object>> getDiscussPost(@PathVariable("discussPostId") int discussPostId) {
        Map<String, Object> map = new HashMap<>();
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        map.put("post", post);
        User user = userService.findUserById(post.getUserId());
        map.put("user", user);
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        map.put("likeCount", likeCount);

        // 点赞状态，如果用户未登录的话则显示为空
        boolean likeStatus = hostHolder.getUser() != null && likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        map.put("likeStatus", likeStatus);

        return ApiResult.success(map);
    }

    @ApiOperation("异步（消息），帖子置顶操作")
    @PostMapping("/top")
    public ApiResult<String> setTop(int id) {
        try {
            discussPostService.updatePostType(id, 1);
            // 同步到ES
            Event event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }

        return ApiResult.success("帖子置顶成功");
    }

    @ApiOperation("异步（消息），帖子加精操作")
    @PostMapping("/wonderful")
    public ApiResult<String> setWonderful(int id) {

        try {
            discussPostService.updatePostStatus(id, 1);
            // 同步到ES
            Event event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);
        /*
        触发将影响帖子分数的事件时，将对应的帖子ID缓存起来，后面定时处理
         */
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, id);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
        return ApiResult.success("帖子加精成功");
    }

    @ApiOperation("异步，帖子删除操作")
    @PostMapping("/delete")
    public ApiResult<String> setDelete(int id) {
        try {
            discussPostService.updatePostStatus(id, 2);
            // 触发删帖事件
            Event event = new Event()
                    .setTopic(TOPIC_DELETE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
        return ApiResult.success("帖子删除成功");
    }
}
