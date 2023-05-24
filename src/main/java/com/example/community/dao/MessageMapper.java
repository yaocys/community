package com.example.community.dao;

import com.example.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yaosu
 */
@Mapper
public interface MessageMapper {

    /**
     * 查询当前用户的会话列表
     * @param userId 用户id
     * @param offset 分页偏移量
     * @param limit 分页条数
     * @return 返回一条最新的私信
     */
    List<Message> selectConversations(@Param("userId") int userId);

    /**
     * 查询当前用户的会话数量
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话所包含的私信列表
     * @param conversationId 会话ID
     */
    List<Message> selectLetters(String conversationId);

    /**
     * 查询某个会话所包含的私信数量
     */
    int selectLetterCount(String conversationId);

    /**
     * 查询未读私信的数量
     * @param userId 用户ID
     * @param conversationId 会话id
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     * 新增消息
     */
    int insertMessage(Message message);

    /**
     * 修改消息的状态
     */
    int updateStatus(List<Integer> ids, int status);

    /**
     * 查询用户关注的某个主题下最新的通知
     */
    Message selectLatestNotice(int userId, String topic);

    /**
     * 查询某个主题所包含的通知数量
     */
    int selectNoticeCount(int userId, String topic);

    /**
     * 查询未读的通知的数量
     */
    int selectNoticeUnreadCount(int userId, String topic);

    /**
     * 系统通知详情
     * 分页查询某个主题所包含的通知列表
     */
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
