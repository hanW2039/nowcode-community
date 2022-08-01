package com.hanw.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author hanW
 * @create 2022-08-01 12:02
 */
public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){
        //用UUID返回一个随机字符串对象后，toString该对象，再替换字符串中的 ‘-’
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5 加密
    public static String md5(String key){
        //org.apache.commons.lang3包下的StringUtils，可以判断是否为空、是否是空字符串等
        if(StringUtils.isBlank(key)){
            return null;
        }
        //md5DigestAsHex()方法中参数要求为byte
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
