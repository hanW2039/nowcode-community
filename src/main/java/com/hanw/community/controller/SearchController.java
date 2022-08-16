package com.hanw.community.controller;

import com.hanw.community.entity.DiscussPost;
import com.hanw.community.entity.Page;
import com.hanw.community.entity.User;
import com.hanw.community.service.ElasticsearchService;
import com.hanw.community.service.LikeService;
import com.hanw.community.service.UserService;
import com.hanw.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.ws.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hanW
 * @create 2022-08-16 14:40
 */
@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @RequestMapping(path="/search",method= RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        //搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getOffSet() , page.getLimit());

        //聚合数据
        List<Map<String,Object>> list = new ArrayList<>();
        if(searchResult != null){
            for(DiscussPost post : searchResult){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(EMTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                list.add(map);
            }
        }
        model.addAttribute("discussPosts",list);
        model.addAttribute("keyword",keyword);
        //分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 :(int)searchResult.getTotalElements());

        return "/site/search";
    }
}
