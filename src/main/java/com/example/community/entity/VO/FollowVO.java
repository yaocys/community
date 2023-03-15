package com.example.community.entity.VO;

import com.example.community.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 21:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowVO {
    private User user;
    private Date followTime;
    private Boolean hasFollow;
}
