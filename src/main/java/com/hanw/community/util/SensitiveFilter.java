package com.hanw.community.util;

import com.sun.xml.internal.fastinfoset.sax.Properties;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import sun.text.normalizer.Trie;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hanW
 * @create 2022-08-06 21:37
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符号
    private static final String REPLACEMENT = "***";

    //根节点初始化
    private TrieNode rootNode = new TrieNode();

    //@PostConstruct 在容器实例化SensitiveFilter类的bean后，备注释的方法自动调用
    @PostConstruct
    public void init(){
        try(
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader =new BufferedReader(new InputStreamReader(is));
        ){
             String keyWord;
             while ((keyWord = reader.readLine()) != null){
                 //添加到前缀树
                 addKeyWord(keyWord);
             }
        }catch(IOException e){
            logger.error("加载铭感词失败：" + e.getMessage());
        }
    }

    /**
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        //结果
        StringBuilder stringBuilder = new StringBuilder();
        while(position < text.length()){
            char c = text.charAt(position);

            //跳过符号
            if(isSymbol(c)){
                //若指针1是处于根节点，将此符号计入节点，让指针2、3向下走一步
                if(tempNode == rootNode){
                    stringBuilder.append(c);
                    begin += 1;
                }
                position += 1;
                continue;
            }
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //以begin开头的不是敏感词
                stringBuilder.append(c);
                position = ++begin;
                //指针归位
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd){
                stringBuilder.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            }else{
                position++;
            }
        }
        //将最后一批字符计入结果
        stringBuilder.append(text.substring(begin));
        return stringBuilder.toString();
    }

    //判断是否为符号
    private boolean isSymbol(char c){
        //isAsciiNumeric()如果为特殊符号返回false
        //0x2E80 ~ 0x9FFF 这个范围内的是东亚文字范围，这个范围外的是特殊符号
        return !CharUtils.isAsciiNumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 将一个词添加到前缀树中
    private void addKeyWord(String keyWord){
        TrieNode n = rootNode;
        int l = keyWord.length();
        for(int i =0;i < l;i++){
            Character c = keyWord.charAt(i);
            TrieNode subNode = n.getSubNode(c);
            if(subNode == null){
                //初始化
                 subNode = new TrieNode();
                n.addSubNodes(c,subNode);
            }
            n = subNode;
            if(i == l - 1){
                n.setKeywordEnd(true);
            }
        }
    }

    //前缀树
    private class TrieNode{
        //关键词结束标志
        private boolean isKeywordEnd = false;
        //子节点(Key是下级字符，value是该字符节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
        //添加子节点
        public void addSubNodes(Character c,TrieNode node){
            subNodes.put(c,node);
        }
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }


}
