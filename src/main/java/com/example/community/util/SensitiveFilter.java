package com.example.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Objects;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    /**
     * 替换符
     * 检测到敏感词就替换成这个
     */
    private static final String REPLACEMENT = "***";

    /**
     * 根节点
     */
    private final TrieNode rootNode = new TrieNode();

    /**
     * 初始化构造一颗前缀树
     * 注解表示：这是一个初始化方法
     * 服务启动时，在容器实例化这个bean，在调用构造方法后，这个方法会被自动调用
     */
    @PostConstruct
    public void init() {
        try (
                // 获取类加载器从类路径下加载敏感词配置文件
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            // 按行读取敏感词，并将敏感词加入到前缀树
            while ((keyword = reader.readLine()) != null) this.addKeyword(keyword);
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    /**
     * 将一个敏感词添加到前缀树中
     *
     * @param keyword 敏感词
     */
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {

            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            // 节点下没有这个字符，就新增一个
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 更新指针指向子节点
            tempNode = subNode;
        }
        tempNode.setKeywordEnd(true);
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {

        if (StringUtils.isBlank(text)) return null;

        StringBuilder sb = new StringBuilder();
        TrieNode tempNode = rootNode;
        int begin = 0, position = 0;

        while (position < text.length()) {
            char c = text.charAt(position);

            // 如果目标字符不在考虑范围内，就跳过
            if (isSymbol(c)) {
                // 如果是非法字符且在判断疑似字符串的过程中，就跳过
                // 不然就当作不是敏感词开头处理
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            tempNode = tempNode.getSubNode(c);

            if (tempNode == null) {
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            } else position++;
        }
        // 如果存在疑似，但是没判定完但是原文本就结束了，需要有一步类似于flush()操作
        sb.append(text.substring(begin));

        return sb.toString();
    }

    /**
     * 判断是否为不合法、不予考虑的字符，为上面的过滤方法做支持
     */
    private boolean isSymbol(char c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 内部类，私有
     * 定义：前缀树节点
     */
    private static class TrieNode {

        /**
         * 关键词结束标识，就是路径从开始到当前节点是不是一个敏感词
         */
        private boolean isKeywordEnd = false;

        /**
         * 子节点(key是下级字符,value是下级节点的引用)
         * 可能有多个子节点
         */
        private final Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }


        /**
         * 添加子节点
         *
         * @param c    字符
         * @param node 前缀树节点
         */
        public void addSubNode(char c, TrieNode node) {
            subNodes.put(c, node);
        }

        /**
         * 获取子节点
         */
        public TrieNode getSubNode(char c) {
            return subNodes.get(c);
        }

    }

/*    public static void main(String[] args) {
        SensitiveFilter sensitiveFilter = new SensitiveFilter();
        sensitiveFilter.init();
        String ss = "嫖娼尽情嫖娼妓？？！开票测试过滤器傻逼";
        System.out.println(sensitiveFilter.filter(ss));
    }*/
}
