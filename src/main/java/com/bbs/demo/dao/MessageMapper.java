package com.bbs.demo.dao;

import com.bbs.demo.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，针对每次会话值返回最新的私信
    List<Message> selectConversations(
            @Param("userId")int userId,@Param("offset") int offset,@Param("limit") int limit);


    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个回话所包含的私信列表
    List<Message> selectLetters(@Param("conversationId")String conversationId,@Param("offset") int offset,@Param("limit") int limit);

    //查询某个回话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    int selectLetterUnreadCount(@Param("userId")int userId,@Param("conversationId") String conversationId);

    //新增消息
    int insertMessage(Message message);

    //修改消息的状态 可以同时修改很多条消息的状态
    int updateStatus(@Param("ids")List<Integer> ids,@Param("status") int status);

    //查询某个主题下的最新的通知
    Message selectLatestNotice(@Param("userId")int userId,@Param("topic") String topic);

    //查询某个主题所包含的通知数量
    int selectNoticeCount(@Param("userId")int userId,@Param("topic") String topic);

    //查询未读的通知的数量
    int selectNoticeUnreadCount(@Param("userId")int userId,@Param("topic") String topic);

    //查寻某个主题所包含的通知的列表
    List<Message> selectNotices(@Param("userId")int userId,@Param("topic") String topic,@Param("offset") int offset,@Param("limit") int limit);
}
