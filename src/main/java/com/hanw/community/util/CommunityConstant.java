package com.hanw.community.util;

/**
 * @author hanW
 * @create 2022-08-01 19:41
 */
public interface CommunityConstant {
    //激活成功
    int ACTIVATION_SUCCESS = 0;
    //重复激活
    int ACTIVATION_REPEAT = 1;
    //激活失败
    int ACTIVATION_FAILURE = 2;
    //默认状态的登陆凭证的超时时间
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    //记住状态下的登陆凭证的超时时间
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /*
    实体类型 帖子
     */
    int EMTITY_TYPE_POST = 1;

    /*
    实体类型 评论
     */
    int EMTITY_TYPE_COMMENT = 2;

    /*
    实体类型 人
     */
    int EMTITY_TYPE_USER = 3;

    //topic 评论
    String TOPIC_COMMENT = "comment";
    //topic 点赞
    String TOPIC_LIKE = "like";
    //topic 关注
    String TOPIC_FOLLOW = "follow";
    //topic 发帖
    String TOPIC_PUBLISH = "publish";

    //系统用户ID
    int SYSTEM_ID = 1;


}
