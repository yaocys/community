package com.example.community.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author yao 2022/4/28
 */
@Data
@ToString
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
    private String openId;
    private String sessionKey;
}
