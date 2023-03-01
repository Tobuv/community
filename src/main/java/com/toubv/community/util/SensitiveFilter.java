package com.toubv.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    public static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    public static final String REPLACEMENT="***";

    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null){
                //加入前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode node = this.rootNode;
        for(char c : keyword.toCharArray()){
            if(node.getSubNode(c) == null){
                node.addSubNode(c, new TrieNode());
            }
            node = node.getSubNode(c);
        }
        node.setKeywordEnd(true);
    }

    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        TrieNode node = rootNode;
        int begin = 0;
        int position = 0;
        StringBuffer sb = new StringBuffer();
        while (position < text.length()){
            char c = text.charAt(position);
            if(isSymbol(c)){
                if(node == rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            node = node.getSubNode(c);
            if(node == null){
                sb.append(text.charAt(begin));
                position = ++begin;
                node=rootNode;
            }else if(node.isKeywordEnd()){
                sb.append(REPLACEMENT);
                begin = ++position;
                node=rootNode;
            }else{
                position++;
            }
        }

        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TrieNode{
        //关键词结束标志
        private boolean isKeywordEnd = false;
        //子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
