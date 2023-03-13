package com.example.community.entity.VO;

import com.example.community.entity.Comment;
import com.example.community.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子回复VO对象
 * @author yaosunique@gmail.com
 * 2023/3/13 19:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyVO {
    private Comment reply;
    private User user;
    private User target;
    private long likeCount;
    private boolean likeStatus;
}
