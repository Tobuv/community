package com.toubv.community.controller;

import com.toubv.community.entity.DiscussPost;
import com.toubv.community.entity.Page;
import com.toubv.community.entity.User;
import com.toubv.community.service.DiscussPostService;
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
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }
}
