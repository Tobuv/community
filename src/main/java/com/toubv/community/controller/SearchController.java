package com.toubv.community.controller;

import com.toubv.community.common.constant.CommentConstant;
import com.toubv.community.entity.DiscussPost;
import com.toubv.community.entity.Page;
import com.toubv.community.service.ElasticsearchService;
import com.toubv.community.service.LikeService;
import com.toubv.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/search")
    public String search(String keyword, Page page, Model model){
        //搜索帖子
        SearchHits<DiscussPost> searchHits = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(!searchHits.isEmpty()){
            for (SearchHit<DiscussPost> searchHit : searchHits) {
                Map<String, Object> map = new HashMap<>();
                DiscussPost post = searchHit.getContent();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                map.put("likeCount", likeService.findEntityLikeCount(CommentConstant.COMMENT_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        page.setPath("search?keyword=" + keyword);
        page.setLimit(10);
        page.setRows(searchHits == null ? 0 :(int) searchHits.getTotalHits());
        return "/site/search";
    }
}
