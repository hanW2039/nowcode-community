package com.hanw.community.controller;

import com.hanw.community.annotation.LoginRequired;
import com.hanw.community.entity.Page;
import com.hanw.community.entity.User;
import com.hanw.community.service.FollowService;
import com.hanw.community.service.UserService;
import com.hanw.community.util.CommunityConstant;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author hanW
 * @create 2022-08-12 11:37
 */
@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
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

    @RequestMapping(path="/followees/{userId}",method=RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);
        page.setRows((int)followService.followeeCount(userId, EMTITY_TYPE_USER));
        page.setPath("/followees/" + user.getId());
        page.setLimit(5);

        List<Map<String, Object>> userList = followService.followeeList(userId, page.getOffSet(), page.getLimit());
        if(userList != null){
            for(Map<String,Object> map : userList){
                User u = (User)map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    @RequestMapping(path="/followers/{userId}",method=RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);
        page.setRows((int)followService.followerCount(userId, EMTITY_TYPE_USER));
        page.setPath("/followers/" + user.getId());
        page.setLimit(5);

        List<Map<String, Object>> userList = followService.followerList(userId, page.getOffSet(), page.getLimit());
        if(userList != null){
            for(Map<String,Object> map : userList){
                User u = (User)map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }
    private boolean hasFollowed(int userId){
        if(hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), EMTITY_TYPE_USER, userId);
    }
}
