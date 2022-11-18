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
     * 初始化一颗前缀树
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
            while ((keyword = reader.readLine()) != null) this.addKeyword(keyword);
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    /**
     * 将一个敏感词添加到前缀树中
     * @param keyword 敏感词
     */
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 如果节点下已经有这个字符了，就跳过
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                // 没有这个字符就把当前字符挂上去
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 更新指针指向子节点,进入下一轮循环
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) tempNode.setKeywordEnd(true);
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) return null;

        // 指针1，指向树中的节点
        TrieNode tempNode = rootNode;
        // 指针2，用以遍历待测字符串
        int begin = 0;
        // 指针3，指向待测子字符串的末尾字符
        int position = 0;
        // 结果，被替换了敏感词的字符串
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            // 得到当前字符
            char c = text.charAt(position);

            // 跳过不在目标过滤范围的符号，同时也是避免敏感词中穿插特殊符号
            if (isSymbol(c)) {
                // 如果开头就是，追加并更新begin
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);

            if (tempNode == null) {
                // 不存在position字符的节点，以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            } else {
                // 有这个节点但是不是结束节点，检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    /**
     * 判断是否为普通字符，为上面的过滤方法做支持
     */
    private boolean isSymbol(Character c) {
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

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        /**
         * 子节点(key是下级字符,value是下级节点)
         * 可能有多个子节点
         */
        private final Map<Character, TrieNode> subNodes = new HashMap<>();

        /**
         * 添加子节点
         * @param c 字符
         * @param node 前缀树节点
         */
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        /**
         * 获取子节点
         */
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }

}
