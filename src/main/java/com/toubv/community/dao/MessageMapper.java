package com.toubv.community.dao;

import com.toubv.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户会话列表，每个会话只展示最近一条记录
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话的私信数目
    int selectLetterCount(String conversation);

    int selectLetterUnreadCount(int userId, String conversationId);

    int insertMessage(Message message);

    int updateMessageStatus(List<Integer> ids, int status);

    //查询某个主题下的最新通知
    Message selectLatestNotice(int userId, String topic);
    //查询某个主题下的通知数量
    int selectNoticeCount(int userId, String topic);
    //查询未读的通知数量
    int selectNoticeUnreadCount(int userId, String topic);
    //查询某通知列表
    List<Message> selectNotice(int userId, String topic, int offset, int limit);
}
