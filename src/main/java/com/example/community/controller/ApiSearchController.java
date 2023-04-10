package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.SearchResult;
import com.example.community.entity.User;
import com.example.community.entity.VO.DiscussPostVO;
import com.example.community.service.ElasticSearchService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 21:48
 */
@Api(tags = "ES搜索API")
@RestController
public class ApiSearchController implements CommunityConstant {

    @Resource
    private ElasticSearchService elasticSearchService;
    @Resource
    private UserService userService;
    @Resource
    private LikeService likeService;

    @ApiOperation("搜索")
    @GetMapping("/search")
    public ApiResult<PageInfo<DiscussPostVO>> search(String keyword, int current, int limit) throws IOException {
        List<DiscussPostVO> discussPostVOList = new ArrayList<>();
        PageHelper.startPage((current - 1) * 10, limit);
        //搜索帖子
        SearchResult searchResult = elasticSearchService.searchDiscussPost(keyword, (current - 1) * 10, limit);
        // 重新封装一份数据，加入用户和点赞数量信息
        List<DiscussPost> list = searchResult.getList();
        if (list != null) {
            for (DiscussPost post : list) {
                User user = userService.findUserById(post.getUserId());
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                DiscussPostVO discussPostVO = new DiscussPostVO(post, user.getUsername(), user.getHeaderUrl(), likeCount);
                discussPostVOList.add(discussPostVO);
            }
        }

        PageInfo<DiscussPostVO> pageInfo = new PageInfo<>(discussPostVOList);

        return ApiResult.success("搜索成功",pageInfo);
    }
}
