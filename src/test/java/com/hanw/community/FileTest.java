package com.hanw.community;

import org.junit.Test;

import java.io.File;

/**
 * @author hanW
 * @create 2022-08-19 18:09
 */
public class FileTest {
    @Test
    public void mkdirTest() {
        File file = new File("e:/IDEACODE/data/wk-images-windows");
        if(!file.exists()) {
            file.mkdir();
            System.out.println("创建成功");
        }
    }
}
