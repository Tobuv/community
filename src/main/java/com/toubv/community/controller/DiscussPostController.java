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
import com.toubv.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.get();
        if(user==null){
            return CommunityUtil.getJSONString(403,"你还没有登录！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        int i = discussPostService.addDiscussPost(post);
        //System.out.println(i);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(CommentConstant.COMMENT_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

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
    // 置顶请求
    @PostMapping(path = "/top")
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        // 触发发帖的事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.get().getId())
                .setEntityType(CommentConstant.COMMENT_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 加精请求
    @PostMapping(path = "/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.get().getId())
                .setEntityType(CommentConstant.COMMENT_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);


        return CommunityUtil.getJSONString(0);
    }

    // 删除请求
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.get().getId())
                .setEntityType(CommentConstant.COMMENT_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
