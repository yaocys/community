package com.example.community.entity.VO;

import com.example.community.entity.Comment;
import com.example.community.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 19:16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {
    private Comment comment;
    private User user;
    private long LikeCount;
    private boolean likeStatus;

    private List<ReplyVO> replies;

    private int replyCount;
}
