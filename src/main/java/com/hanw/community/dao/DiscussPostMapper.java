package com.hanw.community.dao;

import com.hanw.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hanW
 * @create 2022-07-30 19:12
 */
@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    //mapper中的sql需要动态的拼接参数，且参数只有一个，要用@Param()给参数取别名,否则报错
    int selectDiscussPostRows(@Param("userId") int userId);

    //
    int insertDiscussPost(DiscussPost discussPost);
}
