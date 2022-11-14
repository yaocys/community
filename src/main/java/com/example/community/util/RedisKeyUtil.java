package com.example.community.util;

/**
 * @author yao 2022/6/15
 */
public class RedisKeyUtil {
    private static final String SPLIT=":";
    private static final String PREFIX_ENTITY_LIKE="like:entity";

    // 某个实体的赞

    /**
     * like:entity:entityType:entityId->set(userId)
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }
}
