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

    /**
     * 根据id查单个帖子，为”查看帖子详情“功能服务
     * @param id 这边SQL语句回是动态SQL，有时拼id有时不拼
     * @return 返回一个帖子对象
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 分页查询
     * @param userId 用户ID，为个人主页的帖子列表服务，如果没有则是查询首页
     * @param offset 页偏移
     * @param limit 数量
     * @return 返回帖子DiscussPost集合list
     */
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    /**
     * 查询列子行数
     */
    int selectDiscussPostRows(@Param("userId") int userId);
    // 这个疏解的用处是给参数起一个别名
    // 如果只有一个参数，并且在<if>里使用，就必须加别名

    /**
     * 增加帖子
     * @param discussPost 一个帖子对象
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 更新评论数量
     */
    int updateCommentCount(int id, int commentCount);
}
