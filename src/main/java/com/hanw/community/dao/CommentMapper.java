package com.hanw.community.dao;

import com.hanw.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hanW
 * @create 2022-08-07 20:56
 */
@Mapper
public interface CommentMapper {
    //根据帖子类型和帖子号对应的评论
    List<Comment> selectCommentsByEntity(int entityType,int entityId, int offset,int limit);

    int selectCommentRowsByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
