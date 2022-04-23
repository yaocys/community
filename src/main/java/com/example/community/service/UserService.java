package com.example.community.service;

import com.example.community.dao.UserMapper;
import com.example.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yao 2022/4/13
 */
@Service
public class UserService {
    // 根据查询结果中的user_id替换为用户名
    @Autowired
    private UserMapper userMapper;
    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
