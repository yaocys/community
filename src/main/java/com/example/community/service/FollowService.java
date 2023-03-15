package com.example.community.service;

import com.example.community.entity.User;
import com.example.community.entity.VO.FollowVO;
import com.example.community.util.CommunityConstant;
import com.example.community.util.RedisKeyUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 关注业务处理类
 *
 * @author yao 2022/11/22
 */
@Service
public class FollowService implements CommunityConstant {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;

    /**
     * 关注/取关
     *
     * @param userId     发出操作的用户ID
     * @param entityType 被关注的实例类型
     * @param entityId   被关注实体的ID
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();

/*                Double score = operations.opsForZSet().score(followeeKey,entityId);

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
                }*/
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    /**
     * 取关
     * 这里分开写主要是为了后面的关注通知，取关不通知
     */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    /**
     * 查询某用户关注实体的数量
     *
     * @param userId     用户ID
     * @param entityType 关注实体的类型，分开显示
     * @return 关注了多少数量的实体
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);

        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 某用户的粉丝数
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查当前用户是否已经关注实体
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * 分页查询用户关注的人
     */
    public PageInfo<FollowVO> queryFollowee(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        return getFollow(followeeKey, offset, limit);
    }


    /**
     * 分页查询用户的粉丝
     */
    public PageInfo<FollowVO> queryFollower(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        return getFollow(followerKey, offset, limit);
    }

    private PageInfo<FollowVO> getFollow(String key, int offset, int limit) {
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1);

        if (targetIds == null) return new PageInfo<>();

        List<FollowVO> followVOList = new ArrayList<>();
        for (Integer targetId : targetIds) {
            FollowVO followVO = new FollowVO();
            User user = userService.findUserById(targetId);
            followVO.setUser(user);
            // 分数就是时间，毫秒数
            Double score = redisTemplate.opsForZSet().score(key, targetId);
            followVO.setFollowTime(new Date(score.longValue()));
            followVOList.add(followVO);
        }
        return new PageInfo<>(followVOList);
    }
}
