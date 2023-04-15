package com.example.community.entity.VO;

import com.example.community.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 20:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVO {
    private UserVO userVO;
    private int likeCount;
    private long followeeCount;
    private long followerCount;
    private boolean hasFollowed;
}
