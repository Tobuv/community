package com.toubv.community.dao;

import com.toubv.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    /**
     * 查询需要展示的帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     * 查询总帖子总数
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

}
