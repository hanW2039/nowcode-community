package com.hanw.community.service;

import com.hanw.community.dao.LoginTicketMapper;
import com.hanw.community.dao.UserMapper;


import com.hanw.community.entity.LoginTicket;
import com.hanw.community.entity.User;
import com.hanw.community.util.CommunityConstant;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.MailClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.context.SpringContextUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author hanW
 * @create 2022-07-30 20:17
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        // 空值判断
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMessage","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMessage","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMessage","邮箱不能为空");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMessage","该账号已存在");
            return map;
        }
        //验证邮箱
        User e = userMapper.selectByEmail(user.getEmail());
        if(e != null){
            map.put("emailMessage","该邮箱已被注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //url路径
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    public int activation(int userId,String code){
        User u = userMapper.selectById(userId);
        if(u.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(u.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,long expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        //空之判断
        if(StringUtils.isBlank(username)){
            map.put("usernameMessage","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMessage","密码不能为空");
            return map;
        }
        User user = userMapper.selectByName(username);
        //验证账号
        if(null == user){
            map.put("usernameMessage","账号不存在！");
            return map;
        }
        //验证是否激活
        if(user.getStatus() == 0){
            map.put("usernameMessage","该账号未激活！");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMessage","密码错误！");
            return map;
        }
        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds *1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }
    //退出
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }

    //查询凭证
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

    public int updatePassword(int id,String password){
        return userMapper.updatePassword(id,password);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }
}
