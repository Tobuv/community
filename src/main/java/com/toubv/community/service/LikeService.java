package com.toubv.community.service;

import com.toubv.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //like
    public void like(int userId, int entityType, int entityId, int entityUserId){
/*        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        Boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if(member){
            //cancel like
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        }else{
            //like
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }*/
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
                String entityUserKey = RedisUtil.getUserLikeKey(entityUserId);
                boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                operations.multi();
                if(member){
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(entityUserKey);
                }else {
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisTemplate.opsForValue().increment(entityUserKey);
                }

                return operations.exec();
            }
        });
    }

    //cal like counts
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //like status
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    public int findUserLikeCount(int userId){
        String userLikeKey = RedisUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
