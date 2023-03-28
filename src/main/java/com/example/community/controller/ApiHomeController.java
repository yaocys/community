package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.VO.DiscussPostVO;
import com.example.community.service.DiscussPostService;
import com.example.community.service.LikeService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.example.community.util.CommunityConstant.ENTITY_TYPE_POST;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/13 16:47
 */

@Api(tags = "主页API")
@RestController
public class ApiHomeController {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private LikeService likeService;

    @ApiOperation("分页 主页帖子列表，每次传递页码，实现一页一页加载数据")
    @GetMapping(path = "/index")
    public ApiResult<PageInfo<DiscussPostVO>> queryDiscussList(int offset, int limit){
        /*
        这里考虑一个问题，我不应该一次性把所有的数据全部都查出来返回了，而是应该根据前端页面一页一页的加载
        当然这么做主要是考虑到数据量可能很大，一次性加载无论是时间还是空间上来说都不合适
         */
        PageInfo<DiscussPostVO> pageInfo = discussPostService.queryDiscussPosts(offset, limit);

        if(pageInfo.getList()!=null){
            for(DiscussPostVO discussPostVO :pageInfo.getList()){
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostVO.getId());
                discussPostVO.setLikeCount(likeCount);
            }
        }
        return ApiResult.success("首页帖子列表查询成功",pageInfo);
    }

    // TODO 这两个重定向应该交给前端去做，但是后端要给出消息
    @ApiOperation("错误页")
    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    @ApiOperation("拦截/404页")
    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }
}
