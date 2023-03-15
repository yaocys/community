package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.Event;
import com.example.community.entity.User;
import com.example.community.entity.VO.FollowVO;
import com.example.community.event.EventProducer;
import com.example.community.service.FollowService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.HostHolder;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 20:50
 */
@Api(tags = "关注操作API")
@RestController
public class ApiFollowController implements CommunityConstant {
    @Resource
    private FollowService followService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private UserService userService;
    @Resource
    private EventProducer eventProducer;

    // TODO 这里只实现了关注用户，帖子什么的还有待实现

    @ApiOperation("关注某个用户")
    @PostMapping("/follow")
    public ApiResult<String> follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        /*
        触发关注事件，发送消息
         */
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return ApiResult.success("关注成功");
    }

    @ApiOperation("取关某个用户")
    @PostMapping("/unfollow")
    public ApiResult<String> unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return ApiResult.success("已取关");
    }

    @ApiOperation("查看关注列表")
    @GetMapping("/followees/{userId}")
    public ApiResult<PageInfo<FollowVO>> getFollowees(@PathVariable("userId") int userId, int offset, int limit) {
        User user = userService.findUserById(userId);
        if (user == null) return ApiResult.fail("该用户不存在!");
        // 当前用户信息先暂时不放，看后面有没有必要
        // model.addAttribute("user", user);

        PageInfo<FollowVO> pageInfo = followService.queryFollowee(userId, offset, limit);
        hasFollowed(pageInfo);

        return ApiResult.success(pageInfo);
    }

    @ApiOperation("查看粉丝列表")
    @GetMapping(path = "/followers/{userId}")
    public ApiResult<PageInfo<FollowVO>> getFollowers(@PathVariable("userId") int userId, int offset, int limit) {
        User user = userService.findUserById(userId);
        if (user == null) return ApiResult.fail("该用户不存在!");
        // model.addAttribute("user", user);
        PageInfo<FollowVO> pageInfo = followService.queryFollower(userId, offset, limit);
        hasFollowed(pageInfo);

        return ApiResult.success(pageInfo);
    }

    private void hasFollowed(PageInfo<FollowVO> pageInfo) {
        if (pageInfo.getList() != null) {
            for (FollowVO followVO : pageInfo.getList()) {
                boolean followed = false;
                if (hostHolder.getUser() != null)
                    followed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, followVO.getUser().getId());
                followVO.setHasFollow(followed);
            }
        }
    }
}
