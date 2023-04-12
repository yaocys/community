package com.example.community.entity.VO;

import com.example.community.entity.DiscussPost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/13 22:18
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DiscussPostVO extends DiscussPost {

    String username;
    String headerUrl;
    Long likeCount;
    boolean likeStatus;

    public DiscussPostVO(DiscussPost discussPost, String username, String headerUrl, Long likeCount,boolean likeStatus) {
        super(discussPost.getId(),
                discussPost.getUserId(),
                discussPost.getTitle(),
                discussPost.getContent(),
                discussPost.getType(),
                discussPost.getStatus(),
                discussPost.getCreateTime(),
                discussPost.getCommentCount(),
                discussPost.getScore());
        this.username = username;
        this.headerUrl = headerUrl;
        this.likeCount = likeCount;
        this.likeStatus = likeStatus;
    }
}
