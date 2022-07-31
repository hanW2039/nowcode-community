package com.hanw.community;

import com.hanw.community.dao.DiscussPostMapper;
import com.hanw.community.dao.UserMapper;
import com.hanw.community.entity.DiscussPost;
import com.hanw.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author hanW
 * @create 2022-07-30 14:40
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);
        User user1 = userMapper.selectByName("liubei");
        System.out.println(user1);
        User user2 = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user1);
    }

    @Test
    public void testInsertUser(){
        User user = new User(202,"test","test");

        int test = userMapper.insertUser(user);
        System.out.println(test);
        System.out.println(user.getId());
    }
    @Test
    public void testUpdateUser(){
        int test = userMapper.updateStatus(150,1);
        System.out.println(test);
    }

    @Test
    public void testSelectDiscussPosts(){
        List<DiscussPost> discussPostList = discussPostMapper.selectDiscussPosts(0,0,10);
        for(DiscussPost d: discussPostList){
            System.out.println(d);
        }
        int test1 = discussPostMapper.selectDiscussPostRows(101);
        System.out.println(test1);
    }
}
