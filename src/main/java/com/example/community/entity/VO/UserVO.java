package com.example.community.entity.VO;

import com.example.community.entity.User;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于前端存放的用户信息（不包含敏感数据）
 * @author yaosunique@gmail.com
 * 2023/3/17 18:07
 */
@ToString
public class UserVO implements Serializable {
    private final int id;
    private final String username;
    private final String email;
    private final int type;
    private final int status;
    private final String activationCode;
    private final String headerUrl;
    private final Date createTime;

    public UserVO(User user){
        this.id = user.getId();
        this.username=user.getUsername();
        this.email = user.getEmail();
        this.type = user.getType();
        this.status= user.getStatus();
        this.activationCode=user.getActivationCode();
        this.headerUrl = user.getHeaderUrl();
        this.createTime=user.getCreateTime();
    }
}
