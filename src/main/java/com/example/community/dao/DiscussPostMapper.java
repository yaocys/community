package com.example.community.dao;

import com.example.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yao 2022/4/12
 */
@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    int selectDiscussPostRows(@Param("userId") int userId);
    // 这个疏解的用处是给参数起一个别名
    // 如果只有一个参数，并且在<if>里使用，就必须加别名

    /**
     * 增加帖子
     * @param discussPost 一个帖子对象
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 根据id查帖子，为”查看帖子详情“功能服务
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新评论数量
     * @param id
     * @param commentCount
     * @return
     */
    int updateCommentCount(int id, int commentCount);
}
