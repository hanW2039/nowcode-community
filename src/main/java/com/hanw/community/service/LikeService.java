package com.hanw.community.service;

import com.hanw.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author hanW
 * @create 2022-08-11 18:44
 */
@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;
    //点赞功能
    public void like(int userId,int entityType,int entityId,int entityUserId){
        /*重构点赞代码
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        if(isMember){
            redisTemplate.opsForSet().remove(key,userId);
        }else{
            redisTemplate.opsForSet().add(key,userId);
        }*/
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String user = RedisKeyUtil.getEntityLikeUser(entityUserId);
                Boolean isMember = operations.opsForSet().isMember(key, userId);

                operations.multi();
                if(isMember){
                    operations.opsForSet().remove(key,userId);
                    operations.opsForValue().decrement(user);
                }else{
                    operations.opsForSet().add(key,userId);
                    operations.opsForValue().increment(user);
                }
                return operations.exec();
            }
        });




    }

    //查询某实体点赞数量
    public long findEntityLikeCount(int entityType,int entityId){
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key, userId) ? 1 : 0;

    }

    //查询某个用户被赞数量
    public int findUserLikeCount(int userId){
        String user = RedisKeyUtil.getEntityLikeUser(userId);
        Integer count = (Integer)redisTemplate.opsForValue().get(user);
        return count == null? 0 : count.intValue();
    }
}
