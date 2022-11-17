package com.example.community.util;

import com.example.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author yao 2022/5/4
 * 作用是：持有用户信息，以代替session对象
 */
@Component
public class HostHolder {
    private final ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void clear(){
        users.remove();
    }
}
