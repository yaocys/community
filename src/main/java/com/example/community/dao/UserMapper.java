package com.example.community.dao;

import com.example.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yao 2022/4/13
 */
@Mapper
public interface UserMapper {
    User selectById(int id);
    User selectByName(String name);
    User selectByEmail(String email);
    int insertUser(User user);
    int updateStatus(int id,int status);
    int updateHeader(int id,int headerUrl);
    int updatePassword(int id,String password);
}
