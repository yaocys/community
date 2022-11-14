package com.example.community.service;

import com.example.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author yao 2022/6/15
 */
public class LikeService {
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void like(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        if(isMember){
            // 已经点过赞了
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        }else{
            redisTemplate.opsForSet().add(entityLikeKey,userId);
        }
    }

    /**
     * 查询实体点赞的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
    }
}
