package com.hanw.community.controller;

import com.hanw.community.entity.DiscussPost;
import com.hanw.community.entity.User;
import com.hanw.community.service.DiscussPostService;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author hanW
 * @create 2022-08-07 11:55
 */
@Controller
@RequestMapping(path="/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path="/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"你还没有登录！");
        }
        DiscussPost post= new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContext(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0,"发布成功！");
    }

}
