package com.hanw.community.controller;

import com.hanw.community.entity.Comment;
import com.hanw.community.entity.DiscussPost;
import com.hanw.community.entity.Page;
import com.hanw.community.entity.User;
import com.hanw.community.service.CommentService;
import com.hanw.community.service.DiscussPostService;
import com.hanw.community.service.LikeService;
import com.hanw.community.service.UserService;
import com.hanw.community.util.CommunityConstant;
import com.hanw.community.util.CommunityUtil;
import com.hanw.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author hanW
 * @create 2022-08-07 11:55
 */
@Controller
@RequestMapping(path="/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;

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
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @RequestMapping(path="/detail/{discussPostId}",method=RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("post",post);
        model.addAttribute("user",user);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(EMTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), EMTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus",likeStatus);
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(EMTITY_TYPE_POST, post.getId(), page.getOffSet(), page.getLimit());
        //评论的vo列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                Map<String,Object> commentVo = new HashMap<>();
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount = likeService.findEntityLikeCount(EMTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), EMTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);
                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(EMTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复的vo列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply", reply);
                        //作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(EMTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), EMTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(EMTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

}
