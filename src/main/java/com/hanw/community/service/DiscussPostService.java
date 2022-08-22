package com.hanw.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.hanw.community.dao.DiscussPostMapper;
import com.hanw.community.entity.DiscussPost;
import com.hanw.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author hanW
 * @create 2022-07-30 19:57
 */
@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;


    // caffeine的核心接口：Cache，LoadingCache 和 AsyncLoadingCache(异步)
    // 帖子列表的缓存

    private LoadingCache<String, List<DiscussPost>> postListCache;
    // 缓存帖子总数
    private LoadingCache<Integer, Integer> postRowsCache;
    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    //如果缓存不存在,加入缓存
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数为空");
                        }
                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数为空");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        //可加入 二级缓存


                        logger.debug("load post list from DB");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);

                    }
                });
        //初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }



    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        if(orderMode == 1 && userId == 0) {
            //从本地缓存（一级缓存）中获取
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId){
        if(userId == 0) {
            return postRowsCache.get(userId);
        }

        logger.debug("load post rows from DB");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int upadeCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }


    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }
    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }

    public int updateScore(int id,Double score){
        return discussPostMapper.updateScore(id,score);
    }
}
