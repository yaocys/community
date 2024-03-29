package com.example.community.service;

import com.example.community.dao.CommentMapper;
import com.example.community.entity.Comment;
import com.example.community.util.CommunityConstant;
import com.example.community.util.SensitiveFilter;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;

/**
 * @author yaosu
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> queryAllCommentsByEntity(int entityType, int entityId) {
        return commentMapper.selectCommentsByEntity(entityType, entityId);
    }

    public PageInfo<Comment> queryCommentsByEntity(int entityType, int entityId, int offset, int limit){
        PageHelper.startPage(offset,limit);
        List<Comment> commentList= commentMapper.queryCommentsByEntity(entityType, entityId);
        return new PageInfo<>(commentList);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }


    /**
     * 次数包含事务
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 过滤标签
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 过滤敏感词
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
