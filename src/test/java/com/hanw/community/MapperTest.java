package com.hanw.community;

import com.hanw.community.dao.DiscussPostMapper;
import com.hanw.community.dao.LoginTicketMapper;
import com.hanw.community.dao.MessageMapper;
import com.hanw.community.dao.UserMapper;
import com.hanw.community.entity.DiscussPost;
import com.hanw.community.entity.LoginTicket;
import com.hanw.community.entity.Message;
import com.hanw.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sun.security.krb5.internal.Ticket;

import java.util.Date;
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
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private MessageMapper messageMapper;

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
        List<DiscussPost> discussPostList = discussPostMapper.selectDiscussPosts(0,0,10,0);
        for(DiscussPost d: discussPostList){
            System.out.println(d);
        }
        int test1 = discussPostMapper.selectDiscussPostRows(101);
        System.out.println(test1);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket ticket = new LoginTicket();
        ticket.setTicket("1234");
        ticket.setId(1);
        ticket.setStatus(1);
        ticket.setExpired(new Date());
        int i = loginTicketMapper.insertLoginTicket(ticket);
        System.out.println(i);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("1234");
        System.out.println(loginTicket);
    }
    @Test
    public void testUpdateLoginTicket(){
        loginTicketMapper.updateStatus("1234",0);
    }

    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversations(103, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(103);
        System.out.println(count);

        list = messageMapper.selectLetters("101_103", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("101_103");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(103, "101_103");
        System.out.println(count);

    }
}
