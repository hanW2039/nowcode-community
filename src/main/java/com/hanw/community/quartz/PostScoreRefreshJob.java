package com.hanw.community.quartz;

import com.hanw.community.entity.DiscussPost;
import com.hanw.community.service.DiscussPostService;
import com.hanw.community.service.ElasticsearchService;
import com.hanw.community.service.LikeService;
import com.hanw.community.util.CommunityConstant;
import com.hanw.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author hanW
 * @create 2022-08-19 11:41
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    //牛客纪元
    private static final Date epoch;
    static{
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败",e);
        }
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        //如果缓存中不存在变动的帖子，停止任务
        if(operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while(operations.size() > 0) {
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完成：" + operations.size());


    }



    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if(post.getStatus() == 2) {
            logger.error("该帖子已删除");
            return;
        }
        if(post == null) {
            logger.error("该帖子不存在");
            return;
        }
        // 是否是精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数
        int commentCount = post.getCommentCount();
        // 点赞数
        long likeCount = likeService.findEntityLikeCount(EMTITY_TYPE_POST, postId);
        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w,1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        discussPostService.updateScore(postId,score);
        post.setScore(score);
        //同步到搜索数据
        elasticsearchService.saveDiscussPost(post);
    }
}
