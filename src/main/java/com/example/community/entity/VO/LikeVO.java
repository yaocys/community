package com.example.community.entity.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 21:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeVO {
    private long likeCount;
    private boolean likeStatus;
}
