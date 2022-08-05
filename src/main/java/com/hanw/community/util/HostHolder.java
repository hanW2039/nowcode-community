package com.hanw.community.util;

import com.hanw.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author hanW
 * @create 2022-08-04 12:59
 */
/*
    持有用户信息，代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();
    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void clear(){
        users.remove();
    }
}
