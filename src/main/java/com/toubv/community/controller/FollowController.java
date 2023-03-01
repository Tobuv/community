package com.toubv.community.controller;

import com.toubv.community.common.annotation.LoginRequired;
import com.toubv.community.common.constant.CommentConstant;
import com.toubv.community.common.constant.TopicConstant;
import com.toubv.community.entity.Event;
import com.toubv.community.entity.Page;
import com.toubv.community.entity.User;
import com.toubv.community.event.EventProducer;
import com.toubv.community.service.FollowService;
import com.toubv.community.service.UserService;
import com.toubv.community.util.CommunityUtil;
import com.toubv.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements TopicConstant {

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @PostMapping("/follow")
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId){
        User user = hostHolder.get();
        followService.follow(user.getId(), entityType, entityId);

        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注！");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    @LoginRequired
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.get();
        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        //判断该用户是否存在
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        //设置分页
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, CommentConstant.ENTITY_TYPE_USER));
        //查询关注的人
        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(followees != null){
            for (Map<String, Object> followee : followees) {
                User user1 = (User) followee.get("user");
                followee.put("hasFollowed", hasFollowed(user1.getId()));
            }
        }
        model.addAttribute("followees", followees);
        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        //判断该用户是否存在
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        //设置分页
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(CommentConstant.ENTITY_TYPE_USER, userId));
        //查询关注的人
        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(followers != null){
            for (Map<String, Object> follower : followers) {
                User user1 = (User) follower.get("user");
                follower.put("hasFollowed", hasFollowed(user1.getId()));
            }
        }
        model.addAttribute("followers", followers);
        return "/site/follower";
    }

    public boolean hasFollowed(int userId){
        if(hostHolder.get() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.get().getId(), CommentConstant.ENTITY_TYPE_USER, userId);
    }
}
