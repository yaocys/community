package com.example.community.entity;

import java.util.List;

/**
 * 自定义查询结果实体，暂存ES查询记录和数量
 * @author yao 2022/11/30
 */
public class SearchResult {
    private List<DiscussPost> list;
    private long total;
    public SearchResult(List<DiscussPost> list, long total) {
        this.list = list;
        this.total = total;
    }

    public List<DiscussPost> getList() {
        return list;
    }

    public void setList(List<DiscussPost> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
