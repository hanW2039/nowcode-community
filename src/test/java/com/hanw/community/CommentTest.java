package com.hanw.community;

import com.hanw.community.dao.CommentMapper;
import com.hanw.community.entity.Comment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author hanW
 * @create 2022-08-08 10:43
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CommentTest {
    @Autowired
    private CommentMapper commentMapper;
    @Test
    public void test(){
        List<Comment> comments = commentMapper.selectCommentsByEntity(1, 1, 0, 10);
        for(Comment c: comments) {
            System.out.println(c);
        }
    }
}
