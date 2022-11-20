package com.example.community.controller;

import com.example.community.entity.User;
import com.example.community.service.LikeService;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yao 2022/6/16
 */
@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    /**
     * 点赞接口
     * @param entityType 点赞的目标实体类型
     * @param entityId 点赞目标实体类型的ID
     */
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId){
        // TODO 这里后面会用权限管理框架统一处理
        User user = hostHolder.getUser();

        likeService.like(user.getId(), entityType,entityId);

        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        int likeStatus= likeService.findEntityLikeStatus(user.getId(), entityType,entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return CommunityUtil.getJSONString(0, null, map);
    }
}
