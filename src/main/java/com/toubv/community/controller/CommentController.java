package com.toubv.community.controller;

import com.toubv.community.common.constant.CommentConstant;
import com.toubv.community.common.constant.TopicConstant;
import com.toubv.community.entity.Comment;
import com.toubv.community.entity.DiscussPost;
import com.toubv.community.entity.Event;
import com.toubv.community.entity.User;
import com.toubv.community.event.EventProducer;
import com.toubv.community.service.CommentService;
import com.toubv.community.service.DiscussPostService;
import com.toubv.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements TopicConstant{

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        User user = hostHolder.get();//todo:异常处理，权限设置
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(user.getId());

        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TopicConstant.TOPIC_COMMENT)
                .setUserId(user.getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setMap("postId", discussPostId);
        if(comment.getEntityType() == CommentConstant.COMMENT_TYPE_POST){
            DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
            event.setEntityUserId(discussPost.getUserId());
        }else if(comment.getEntityType() == CommentConstant.COMMENT_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());

        }
        eventProducer.fireEvent(event);

        if(comment.getEntityType() == CommentConstant.COMMENT_TYPE_POST){
            //触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(user.getId())
                    .setEntityType(CommentConstant.COMMENT_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

        }
        return "redirect:/discuss/detail/" + discussPostId;
    }

}
