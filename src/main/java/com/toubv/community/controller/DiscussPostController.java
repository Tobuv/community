package com.toubv.community.controller;

import com.toubv.community.common.constant.CommentConstant;
import com.toubv.community.common.constant.TopicConstant;
import com.toubv.community.entity.*;
import com.toubv.community.event.EventProducer;
import com.toubv.community.service.CommentService;
import com.toubv.community.service.DiscussPostService;
import com.toubv.community.service.LikeService;
import com.toubv.community.service.UserService;
import com.toubv.community.util.CommunityUtil;
import com.toubv.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements TopicConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;


    @GetMapping("/detail/{id}")
    public String getDiscussPost(Model model, @PathVariable("id") int id, Page page){
        //post
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post", post);
        //author
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //likeCount
        long likeCount = likeService.findEntityLikeCount(CommentConstant.COMMENT_TYPE_POST, id);
        model.addAttribute("likeCount", likeCount);
        //likeStatus
        int likeStatus = (hostHolder.get() == null) ? 0 : likeService.findEntityLikeStatus(hostHolder.get().getId(), CommentConstant.COMMENT_TYPE_POST, id);
        model.addAttribute("likeStatus", likeStatus);
        //TODO:comments
        page.setLimit(5);
        page.setPath("/discuss/detail/" + post.getId());
        page.setRows(post.getCommentCount());

        List<Comment> commentList = commentService.findCommentByEntity(CommentConstant.COMMENT_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for (Comment comment : commentList) {
                //帖子评论
                Map<String, Object> commentVo = new HashMap<>();
                //comment
                commentVo.put("comment", comment);
                //author
                User user1 = userService.findUserById(comment.getUserId());
                commentVo.put("user", user1);
                //likeCount
                likeCount = likeService.findEntityLikeCount(CommentConstant.COMMENT_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //likeStatus
                likeStatus = hostHolder.get() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.get().getId(), CommentConstant.COMMENT_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);
                //评论回复
                List<Comment> ReplyList = commentService.findCommentByEntity(CommentConstant.COMMENT_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> ReplyVoList = new ArrayList<>();
                if(ReplyList != null){
                    for (Comment reply : ReplyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        User user2 = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", user2);
                        //likeCount
                        likeCount = likeService.findEntityLikeCount(CommentConstant.COMMENT_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        //likeStatus
                        likeStatus = hostHolder.get() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.get().getId(), CommentConstant.COMMENT_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        ReplyVoList.add(replyVo);
                    }
                }
                commentVo.put("replies", ReplyVoList);
                int count = commentService.findCountByEntity(CommentConstant.COMMENT_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", count);

                commentVoList.add(commentVo);
            }
            model.addAttribute("comments", commentVoList);
        }

        return "/site/discuss-detail";
    }


}
