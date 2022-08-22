package com.hanw.community.controller;

import com.hanw.community.entity.Comment;
import com.hanw.community.entity.Event;
import com.hanw.community.event.EventProducer;
import com.hanw.community.service.CommentService;
import com.hanw.community.service.DiscussPostService;
import com.hanw.community.util.CommunityConstant;
import com.hanw.community.util.HostHolder;
import com.hanw.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author hanW
 * @create 2022-08-09 19:01
 */
@Controller
@RequestMapping(path="/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path="/add/{discussPostId}",method= RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setCreateTime(new Date());
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        if(comment.getEntityType() == EMTITY_TYPE_POST){
            event.setEntityUserId(discussPostService.findDiscussPostById(comment.getEntityId()).getUserId());
        }else if(comment.getEntityType() == EMTITY_TYPE_COMMENT){
            event.setEntityUserId(commentService.findCommentById(comment.getEntityId()).getUserId());
        }
        eventProducer.fireEvent(event);


        if(comment.getEntityType() == EMTITY_TYPE_POST){
           event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(EMTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
            //计算帖子的分数
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey,discussPostId);
        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
