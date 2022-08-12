package com.hanw.community.controller;

import com.hanw.community.annotation.LoginRequired;
import com.hanw.community.entity.User;
import com.hanw.community.service.LikeService;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.HostHolder;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hanW
 * @create 2022-08-11 19:38
 */
@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @RequestMapping(path="/like",method= RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String like(int entityType,int entityId,int entityUserId){
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(), entityType,entityId,entityUserId);
        //数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);
        return CommunityUtil.getJSONString(0,null,map);
    }
}
