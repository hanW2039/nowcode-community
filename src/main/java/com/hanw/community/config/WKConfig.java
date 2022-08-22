package com.hanw.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author hanW
 * @create 2022-08-19 15:36
 */

@Configuration
public class WKConfig {
    private static final Logger logger = LoggerFactory.getLogger(WKConfig.class);
    @Value("${wk.image.storage}")
    private String wkImageStorage;
    //初始化

    @PostConstruct
    public void init() {
        //创建wk图片目录
        File file = new File(wkImageStorage);
        if(!file.exists()) {
            file.mkdir();
            logger.info("创建wk图片目录：" + wkImageStorage);
        }
    }
}
