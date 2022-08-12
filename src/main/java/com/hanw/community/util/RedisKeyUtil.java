package com.hanw.community.util;

import com.sun.javafx.css.StyleCacheEntry;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author hanW
 * @create 2022-08-11 18:32
 */
public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    //某个实体的赞
    //like:entity:entityTpye:entityId
    public static String getEntityLikeKey(int entityType,int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    public static String getEntityLikeUser(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
}
