package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.VO.DiscussPostVO;
import com.example.community.service.DiscussPostService;
import com.example.community.service.LikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.community.util.CommunityConstant.ENTITY_TYPE_POST;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/13 16:47
 */

@Api(tags = "前后端分离-主页API")
@RestController("/ApiHomeController")
public class ApiHomeController {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private LikeService likeService;

    @ApiOperation("分页 主页帖子列表，每次传递页码，实现一页一页加载数据")
    @GetMapping(path = "/apiIndex")
    public ApiResult<List<Map<String,Object>>> queryDiscussList(int offset,int limit){
        /*
        这里考虑一个问题，我不应该一次性把所有的数据全部都查出来返回了，而是应该根据前端页面一页一页的加载
        当然这么做主要是考虑到数据量可能很大，一次性加载无论是时间还是空间上来说都不合适
         */
        List<DiscussPostVO> list = discussPostService.queryDiscussPosts(offset, limit);

        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for(DiscussPostVO discussPostVO:list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",discussPostVO);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostVO.getId());
                map.put("like",likeCount);
                discussPosts.add(map);
            }
        }
        return ApiResult.success("首页帖子列表查询成功",discussPosts);
    }
}
