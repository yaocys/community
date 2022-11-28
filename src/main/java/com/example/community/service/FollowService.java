package com.example.community.service;

import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 关注业务处理类
 * @author yao 2022/11/22
 */
@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 关注/取关
     * @param userId 发出操作的用户ID
     * @param entityType 被关注的实例类型
     * @param entityId 被关注实体的ID
     */
    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                Double score = operations.opsForZSet().score(followeeKey,entityId);

                // 如果用户的关注列表中没有这一项，就关注
                // 不然就是取关
                if(score==null){
                    // 为用户的关注列表添加一项
                    operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                    // 为实体的被关注列表添加一个用户
                    operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                }else{
                    operations.opsForZSet().remove(followeeKey,entityId);
                    operations.opsForZSet().remove(followerKey,userId);
                }

                return operations.exec();
            }
        });
    }

    /**
     * 查询某用户关注实体的数量
     * @param userId 用户ID
     * @param entityType 关注实体的类型，分开显示
     * @return 关注了多少数量的实体
     */
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);

        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    public long findFollowerCount(int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return  redisTemplate.opsForZSet().score(followeeKey,entityType)!=null;
    }
}
