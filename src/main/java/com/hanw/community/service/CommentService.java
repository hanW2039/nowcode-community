package com.hanw.community.service;

import com.hanw.community.dao.CommentMapper;
import com.hanw.community.entity.Comment;
import com.hanw.community.util.CommunityConstant;
import com.hanw.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author hanW
 * @create 2022-08-08 12:12
 */
@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType,int entityId, int offset,int limit){
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    public int findCommentCount(int entityType,int entityId){
        return commentMapper.selectCommentRowsByEntity(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //转义HTML标记
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        int i = commentMapper.insertComment(comment);
        //更新帖子的评论数量
        if(comment.getEntityType() == EMTITY_TYPE_POST){
            int count = commentMapper.selectCommentRowsByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.upadeCommentCount(comment.getEntityId(),count);
        }

        return i;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

    public List<Comment> findCommentsByUserId(int userId, int offset, int limit) {
        return commentMapper.selectCommentsByUserId(userId, offset, limit);
    }
    public int findCommentRowsByUserId(int userId) {
        return commentMapper.selectCommentRowsByUserId(userId);
    }
}
