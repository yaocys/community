package com.example.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.community.common.ApiResult;
import com.example.community.entity.Message;
import com.example.community.entity.User;
import com.example.community.entity.VO.MessageVO;
import com.example.community.service.MessageService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.HostHolder;
import com.example.community.util.PageInfoUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 21:47
 */
@Api(tags = "私信API")
@RestController
public class ApiMessageController implements CommunityConstant {
    @Resource
    private MessageService messageService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @ApiOperation("分页私信列表")
    @GetMapping("/letter/list")
    public ApiResult<?> getLetterList(int offset, int limit) {
        User user = hostHolder.getUser();

        PageInfo<Message> pageInfo = messageService.findConversations(user.getId(), offset, limit);

        List<MessageVO> messageVOList = new ArrayList<>();
        if (pageInfo.getList() != null) {
            for (Message message : pageInfo.getList()) {
                MessageVO messageVO = new MessageVO(message);
                messageVO.setLetterCount(messageService.findLetterCount(message.getConversationId()));
                messageVO.setUnreadCount(messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                // 用到的是目标用户头像和用户名还有userId
                messageVO.setTarget(userService.findUserById(targetId));

                messageVOList.add(messageVO);
            }
        }
        PageInfo<MessageVO> targetPageInfo = PageInfoUtil.convertPageInfo(pageInfo, MessageVO.class);
        targetPageInfo.setList(messageVOList);

        // 最终返回的数据集合
        Map<String, Object> messageList = new HashMap<>();

        messageList.put("conversations", targetPageInfo);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        messageList.put("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        messageList.put("noticeUnreadCount", noticeUnreadCount);

        return ApiResult.success(messageList);
    }

    /**
     * 拆分conversationId，查询目标用户
     *
     * @return 目标User
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    /**
     * 找有哪些未读消息的ID
     */
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                // 当前用户是接收者且消息未读
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    @ApiOperation("私信详情")
    @GetMapping("/letter/detail/{conversationId}")
    public ApiResult<?> getLetterDetail(@PathVariable("conversationId") String conversationId, int offset, int limit) {
        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, offset, limit);

        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                // 显示消息发送人的头像，可能是当前用户，也有可能是目标用户
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("letters", letters);

        // 私信目标（来自XXX的私信）
        resultMap.put("target", getLetterTarget(conversationId));

        // 用户打开页面就把未读的消息设置为已读
        List<Integer> ids = getLetterIds(letterList);

        if (!ids.isEmpty()) messageService.readMessage(ids);

        return ApiResult.success("对话列表查询成功", resultMap);
    }

    @ApiOperation("发送私信")
    @PostMapping("/letter/send")
    public ApiResult<?> sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) return ApiResult.fail("目标用户不存在");

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return ApiResult.success("私信发送成功");
    }

    @ApiOperation("系统消息列表")
    @GetMapping("/notice/list")
    public ApiResult<?> getNoticeList() {

        User user = hostHolder.getUser();
        Map<String, Object> resultMap = new HashMap<>();
        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);

            resultMap.put("commentNotice", messageVO);
        }

        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);

            resultMap.put("likeNotice", messageVO);
        }

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);

            resultMap.put("followNotice", messageVO);
        }

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        resultMap.put("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        resultMap.put("noticeUnreadCount", noticeUnreadCount);

        return ApiResult.success("系统消息列表查询成功", resultMap);
    }

    @ApiOperation("分页查询系统通知详情列表")
    @GetMapping("/notice/detail/{topic}")
    public ApiResult<?> getNoticeDetail(@PathVariable("topic") String topic, int offset, int limit) {
        User user = hostHolder.getUser();

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, offset, limit);

        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者，其实就是SYSTEM
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }

        // 用户查看过就设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return ApiResult.success("分页查询系统通知详情列表成功", noticeVoList);
    }
}
