package com.example.community.entity.VO;

import com.example.community.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于前端存放的用户信息（不包含敏感数据）
 *
 * @author yaosunique@gmail.com
 * 2023/3/17 18:07
 */
@ToString
@Data
@NoArgsConstructor
public class UserVO implements Serializable {
    private int id;
    private String username;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;

    public UserVO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.type = user.getType();
        this.status = user.getStatus();
        this.activationCode = user.getActivationCode();
        this.headerUrl = user.getHeaderUrl();
        this.createTime = user.getCreateTime();
    }
}
