package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.Comment;
import com.example.community.entity.User;
import com.example.community.entity.VO.CommentVO;
import com.example.community.entity.VO.ReplyVO;
import com.example.community.event.EventProducer;
import com.example.community.service.CommentService;
import com.example.community.service.DiscussPostService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.HostHolder;
import com.example.community.util.PageInfoUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.example.community.util.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.example.community.util.CommunityConstant.ENTITY_TYPE_POST;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 18:07
 */
@Api(tags = "评论API")
@RestController
@RequestMapping("/comment")
public class ApiCommentController {
    @Resource
    private CommentService commentService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private LikeService likeService;

    @ApiOperation("获取分页的帖子评论")
    @GetMapping("/query/{discussPostId}")
    public ApiResult<PageInfo<CommentVO>> queryComment(@PathVariable("discussPostId") int discussPostId, int offset, int limit) {
        // TODO 这里是不是有很多的冗余数据
        // 当前帖子的所有评论列表
        // List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, discussPostId, offset, limit);
        PageInfo<Comment> pageInfo = commentService.queryCommentsByEntity(ENTITY_TYPE_POST, discussPostId, offset, limit);
        List<Comment> commentList = pageInfo.getList();
        // 嵌套第一层，所有的评论
        List<CommentVO> commentVOList = new ArrayList<>();

        if (commentList != null) {
            // 遍历每一条评论，查它的回复并组合起来
            for (Comment comment : commentList) {

                CommentVO commentVO = new CommentVO();
                commentVO.setComment(comment);
                commentVO.setUser(userService.findUserById(comment.getUserId()));

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.setLikeCount(likeCount);

                boolean likeStatus = hostHolder.getUser() != null && likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.setLikeStatus(likeStatus);

                // 查询回复列表，这里没有分页
                List<Comment> replyList = commentService.queryAllCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId());

                List<ReplyVO> replyVOList = new ArrayList<>();
                // 嵌套第二层，所有评论的回复
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        ReplyVO replyVO = new ReplyVO();
                        replyVO.setReply(reply);
                        replyVO.setUser(userService.findUserById(reply.getUserId()));

                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVO.setTarget(target);
                        replyVO.setLikeCount(likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId()));

                        likeStatus = hostHolder.getUser() != null && likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.setLikeStatus(likeStatus);

                        replyVOList.add(replyVO);
                    }
                }
                // 评论的回复放到评论里去
                commentVO.setReplies(replyVOList);

                // 评论的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.setReplyCount(replyCount);

                // 评论放到评论列表中去
                commentVOList.add(commentVO);
            }
        }
        PageInfo<CommentVO> targetPageInfo = PageInfoUtil.convertPageInfo(pageInfo, CommentVO.class);
        targetPageInfo.setList(commentVOList);
        return ApiResult.success(targetPageInfo);
    }
}
