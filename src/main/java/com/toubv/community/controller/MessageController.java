package com.toubv.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.sun.jndi.cosnaming.CNCtx;
import com.toubv.community.common.constant.TopicConstant;
import com.toubv.community.entity.Message;
import com.toubv.community.entity.Page;
import com.toubv.community.entity.User;
import com.toubv.community.service.MessageService;
import com.toubv.community.service.UserService;
import com.toubv.community.util.CommunityUtil;
import com.toubv.community.util.HostHolder;
import org.apache.kafka.clients.consumer.StickyAssignor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/letter/list")
    public String getLetterPage(Model model, Page page){

        User user = hostHolder.get();
        //分页设置
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> messageList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if(messageList != null){
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnReadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnReadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //查询通知未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);


        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}/{targetId}")
    public String getLetterPage(@PathVariable("conversationId") String conversationId, Page page, Model model, @PathVariable("targetId") int targetId){
        //分页设计
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId + "/" + targetId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if(letterList != null){
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        model.addAttribute("target", userService.findUserById(targetId));

        //已读设置
        List<Integer> ids = getUnreadList(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getUnreadList(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if(letterList != null){
            for (Message message : letterList) {
                if(message.getToId() == hostHolder.get().getId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String sendMessage(String toName, String content){

        //int x = 1/0;
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }
        Message message = new Message();
        message.setToId(target.getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setFromId(hostHolder.get().getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else{
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);

    }

    @GetMapping("/notice/list")
    public String getNoticePage(Model model){
        User user = hostHolder.get();

        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TopicConstant.TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        messageVo.put("message",message);
        if(message != null){


            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TopicConstant.TOPIC_COMMENT);
            messageVo.put("count", count);
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TopicConstant.TOPIC_COMMENT);
            messageVo.put("unreadCount", unreadCount);
        }

        model.addAttribute("commentNotice", messageVo);

        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TopicConstant.TOPIC_LIKE);
        messageVo = new HashMap<>();
        messageVo.put("message",message);
        if(message != null){


            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TopicConstant.TOPIC_LIKE);
            messageVo.put("count", count);
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TopicConstant.TOPIC_LIKE);
            messageVo.put("unreadCount", unreadCount);
        }

        model.addAttribute("likeNotice", messageVo);

        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TopicConstant.TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        messageVo.put("message",message);
        if(message != null){


            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));


            int count = messageService.findNoticeCount(user.getId(), TopicConstant.TOPIC_FOLLOW);
            messageVo.put("count", count);
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TopicConstant.TOPIC_FOLLOW);
            messageVo.put("unreadCount", unreadCount);
        }

        model.addAttribute("followNotice", messageVo);

        //查询私信未读数量
        int letterUnreadCount = messageService.findLetterUnReadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //查询通知未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";

    }

    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page){
        User user = hostHolder.get();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticesVoList = new ArrayList<>();

        if(notices != null){
            for (Message notice : notices) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityId", data.get("entityId"));
                map.put("entityType", data.get("entityType"));
                map.put("postId", data.get("postId"));
                //通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticesVoList.add(map);
            }
        }

        model.addAttribute("notices", noticesVoList);
        //设置已读
        List<Integer> ids = getUnreadList(notices);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
