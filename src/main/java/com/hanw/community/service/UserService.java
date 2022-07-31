package com.hanw.community.service;

import com.hanw.community.dao.UserMapper;


import com.hanw.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hanW
 * @create 2022-07-30 20:17
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
