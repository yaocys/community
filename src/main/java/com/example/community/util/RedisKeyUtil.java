package com.example.community.util;

/**
 * 生成Redis key工具类
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    /**
     * 前缀，被关注者
     */
    private static final String PREFIX_FOLLOWEE = "followee";
    /**
     * 前缀，关注者
     */
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";


    /**
     * 用set存某个实例收到的赞（保存点赞用户的ID），需要知道是谁点的赞
     * like:entity:entityType:entityId -> set(userId)
     * @param entityType 被点赞的实体类型
     * @param entityId 被点赞实体类型的ID
     * @return like:entity:entityType:entityId
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户收到的赞
     * like:user:userId -> int
     * @param userId 被赞的用户ID
     * @return key名
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注了哪些实体的zset的key
     * followee:userId:entityType -> zset(entityId,now)
     * @param userId 关注者
     * @param entityType 实体类型
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体拥有的粉丝的zet的key
     * follower:entityType:entityId -> zset(userId,now)
     * @param entityType 实体类型
     * @param entityId 被关注的实体
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 获取 登录验证码的key
     * @param owner 验证码的拥有者
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 获取 登录凭证的key
     * @param ticket 登录凭证
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 获取 缓存中用户信息的key
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

}
