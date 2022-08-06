package com.hanw.community;

import com.hanw.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author hanW
 * @create 2022-08-06 23:42
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String test = "*开*票*这里可以赌博可以吸毒可以嫖娼哈哈哈哈哈哈";
        String filter = sensitiveFilter.filter(test);
        System.out.println(filter);
    }
}
