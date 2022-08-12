package com.hanw.community.controller;

import com.hanw.community.annotation.LoginRequired;
import com.hanw.community.entity.User;
import com.hanw.community.service.FollowService;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hanW
 * @create 2022-08-12 11:37
 */
@Controller
public class FollowController {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path="/follow",method= RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(entityType,entityId,user.getId());
        return CommunityUtil.getJSONString(0,"已关注");
    }

    @RequestMapping(path="/unfollow",method= RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(entityType,entityId,user.getId());
        return CommunityUtil.getJSONString(0,"已取关");
    }
}
