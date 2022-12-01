package com.example.community.service;

import com.example.community.dao.DiscussPostMapper;
import com.example.community.entity.DiscussPost;
import com.example.community.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.ibatis.annotations.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author yao 2022/4/13
 */
@Service
public class DiscussPostService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSecond;

    /**
     * 帖子列表缓存
     */
    private LoadingCache<String ,List<DiscussPost>> postListCache;

    /**
     * 帖子总数缓存
     */
    private LoadingCache<Integer,Integer> postRowsCache;

    /**
     * 初始化帖子列表缓存和帖子总数缓存
     */
    @PostConstruct
    public void init(){
        //
    }

    /**
     * 这里应该是包含用户信息的，根据userId
     * 可以在mybatis时关联，也可以分开
     * 这里分开是为了后面Redis缓存方便
     */
    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);
    }

    /**
     * 返回帖子总行数
     */
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 新增帖子会对帖子标题和内容进行过滤和转义
     */
    public int addDiscussPost(DiscussPost discussPost){
        if (discussPost==null){
            throw new IllegalArgumentException("帖子参数不能为空！");
        }
        // 转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updatePostType(int id,int type){
        return discussPostMapper.updatePostType(id, type);
    }

    public int updatePostStatus(int id,int status){
        return discussPostMapper.updatePostStatus(id,status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
