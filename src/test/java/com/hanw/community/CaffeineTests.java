package com.hanw.community;

import com.hanw.community.CommunityApplication;
import com.hanw.community.entity.DiscussPost;
import com.hanw.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author hanW
 * @create 2022-08-20 15:11
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {
    @Autowired
    private DiscussPostService discussPostService;
    @Test
    public void initDataForTest(){
        for(int i = 0; i < 100000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(103);
            post.setTitle("王涵");
            post.setContent("使用谷歌提供的ConcurrentLinkedHashMap有个漏洞，那就是缓存的过期只会发生在缓存达到上限的情况，否则便只会一直放在缓存中。咋一看，这个机制没问题，是没问题，可是却不合理，举个例子，有玩家上线后加载了一堆的数据放在缓存中，之后便不再上线了，那么这份缓存便会一直存在，知道缓存达到上限。\n" +
                    "ConcurrentLinkedHashMap没有提供基于时间淘汰时间的机制，而Caffeine有，并且有多种淘汰机制，并且支持淘汰通知。\n" +
                    "目前Spring也在推荐使用，Caffeine 因使用 Window TinyLfu 回收策略，提供了一个近乎最佳的命中率。");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.addDiscussPost(post);
        }
    }


}
