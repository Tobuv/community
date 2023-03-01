package com.toubv.community.service;

import com.toubv.community.common.constant.CommentConstant;
import com.toubv.community.entity.User;
import com.toubv.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisUtil.getFollowerKey(entityType, entityId);
                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();

            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisUtil.getFollowerKey(entityType, entityId);
                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();

            }
        });
    }
    //查询关注的实体数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询粉丝数量
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否关注某实体
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    //查询某用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit){
        String followeeKey = RedisUtil.getFolloweeKey(userId, CommentConstant.ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if(targetIds == null){
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for(int targetId : targetIds){
            Map<String, Object> map = new HashMap<>();
            User target = userService.findUserById(targetId);
            map.put("user", target);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit){
        String followerKey = RedisUtil.getFollowerKey( CommentConstant.ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if(targetIds == null){
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for(int targetId : targetIds){
            Map<String, Object> map = new HashMap<>();
            User target = userService.findUserById(targetId);
            map.put("user", target);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

}
