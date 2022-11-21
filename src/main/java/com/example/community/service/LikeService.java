package com.example.community.service;

import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author yao 2022/6/15
 */
@Service
public class LikeService {
    /**
     * Redis操作工具类
     */
    @Autowired
    private RedisTemplate redisTemplate;
    // TODO 这里的警告是什么意思？

    /**
     * 点赞
     * @param userId 哪个用户点的赞
     * @param entityType 点赞的目标对象类型
     * @param entityId 点赞目标对象的ID
     */
    public void like(int userId,int entityType,int entityId,int entityAuthorId){
        // TODO 这里要不要做一个Redis持久化的备份？不然Redis一旦关机就数据全丢了
        // Redis有自动的持久化吗？我重启服务器发现数据居然还在？
        /*
         * 这里做了一次重构
         * 主要是一开始只是向帖子、评论、回复实体的set中添加点赞者的ID
         * 现在要记录这些实体的作者收到的赞的数量，这样就不再是单一对象的增删查改
         * 添加了事务支持
         */
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 被点赞实体的key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // 被点赞实体作者的key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityAuthorId);
                boolean isMember = operations.opsForSet().isMember(entityLikeKey,userId);

                operations.multi();

                if(isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    /**
     * 查询实体点赞的数量
     * @param entityType 目标实体类型
     * @param entityId 慕白哦实体类型的ID
     * @return 点赞数量
     */
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对实体的点赞状态
     * @param userId 做出点赞操作的用户ID
     * @param entityType 点赞目标实体类型
     * @param entityId 目标实体类型的ID
     * @return 0/1，是否已经点赞
     */
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        // TODO 这个方法就不能和上面的like合二为一吗？
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
    }

    /**
     * 查询某个用户获得的赞的数量
     * @param userId 用户ID
     * @return 用户获得赞的数量
     */
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0: count;
    }
}
