package com.hanw.community.dao;

import com.hanw.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author hanW
 * @create 2022-08-09 23:47
 */
@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表，针对每个会话只返回一条最新的消息
    List<Message> selectConversations(int userId,int offSet,int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信
    List<Message> selectLetters(String conversationId,int offSet,int limit);

    //查询某个会话包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    int selectLetterUnreadCount(int userId,String conversationId);

    //新增消息
    int insertMessage(Message message);

    //修改私信状态
    int updateStatus(List<Integer> ids,int status);
}
