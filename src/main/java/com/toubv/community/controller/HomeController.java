package com.toubv.community.controller;

import com.toubv.community.common.constant.CommentConstant;
import com.toubv.community.entity.DiscussPost;
import com.toubv.community.entity.Page;
import com.toubv.community.entity.User;
import com.toubv.community.service.DiscussPostService;
import com.toubv.community.service.LikeService;
import com.toubv.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        int offset = page.getOffset();
        int limit = page.getLimit();
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> posts = discussPostService.findDiscussPosts(0, offset, limit);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(posts != null){
            for(DiscussPost post : posts){
                Map<String, Object> map = new HashMap<>();
                //post
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                //author
                map.put("user", user);
                //likeCount
                long likeCount = likeService.findEntityLikeCount(CommentConstant.COMMENT_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }
}
