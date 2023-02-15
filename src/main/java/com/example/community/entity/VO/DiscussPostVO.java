package com.example.community.entity.VO;

import com.example.community.entity.DiscussPost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/13 22:18
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DiscussPostVO extends DiscussPost {

    String username;
    String headerUrl;

    Long likeCount;
}
