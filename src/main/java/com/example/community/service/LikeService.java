package com.example.community.service;

import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    public void like(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 判断在不在集合中，在就已经点过赞，不然就没点过
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        if(isMember) redisTemplate.opsForSet().remove(entityLikeKey,userId);
        else redisTemplate.opsForSet().add(entityLikeKey,userId);
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
}
