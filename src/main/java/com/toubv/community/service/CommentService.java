package com.toubv.community.service;

import com.toubv.community.common.constant.CommentConstant;
import com.toubv.community.dao.CommentMapper;
import com.toubv.community.entity.Comment;
import com.toubv.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    public int findCountByEntity(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        //空值判断
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //敏感词过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //添加评论
        int rows = commentMapper.insertComment(comment);
        //更新帖子评论数量
        if(comment.getEntityType() == CommentConstant.COMMENT_TYPE_POST){
            int num = commentMapper.selectCountByEntity(CommentConstant.COMMENT_TYPE_POST, comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), num);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
}
