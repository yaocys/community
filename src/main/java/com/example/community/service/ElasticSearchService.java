package com.example.community.service;

import com.alibaba.fastjson.JSONObject;
import com.example.community.dao.elasticsearch.DiscussPostRepository;
import com.example.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.community.entity.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yao 2022/11/30
 */
@Service
public class ElasticSearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    // @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;

    /**
     * 向ES数据库添加帖子
     */
    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }

    /**
     * 删除ES数据库的帖子
     */
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    /**
     * 搜索帖子并高亮关键词
     */
    public SearchResult searchDiscussPost(String keyword,int offset,int limit) throws IOException {
        // 指定搜索请求的索引名，即表名
        SearchRequest searchRequest = new SearchRequest("discusspost");

        /*
        配置高亮
         */
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
/*        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");*/
        highlightBuilder.preTags("<text style='color:red'>");
        highlightBuilder.postTags("</text>");

        /*
        构建搜索条件
         */
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                .sort(SortBuilders.fieldSort("type").order((SortOrder.DESC)))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(offset)// 指定从哪条开始查询
                .size(limit)// 需要查出的总记录条数
                .highlighter(highlightBuilder);//高亮
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse  =restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> highlightDiscussPostList = new ArrayList<>();
        long totalNum = searchResponse.getHits().getTotalHits().value;

        for(SearchHit searchHit: searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(searchHit.getSourceAsString(),DiscussPost.class);

            /*
            处理高亮显示结果，使用被高亮的结果覆盖原字段
             */
            HighlightField titleField = searchHit.getHighlightFields().get("title");
            if (titleField != null)
                discussPost.setTitle(titleField.getFragments()[0].toString());

            HighlightField contentField = searchHit.getHighlightFields().get("content");
            if (contentField != null)
                discussPost.setContent(contentField.getFragments()[0].toString());

            highlightDiscussPostList.add(discussPost);
        }
        return new SearchResult(highlightDiscussPostList,totalNum);
    }
}
