package com.hanw.community.dao.elasticsearch;

import com.hanw.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hanW
 * @create 2022-08-15 22:55
 */

/*
    @Mapper 是mybatis专属注解
    @Repository 是Spring提供的数据访问注解
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
