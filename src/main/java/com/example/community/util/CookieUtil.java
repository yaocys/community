package com.example.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 遍历请求携带的cookie列表，获取到指定key的cookie
 * @author yao 2022/5/4
 */
public class CookieUtil {
    public static String getValue(HttpServletRequest request,String name){
        if (request == null || name == null){
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null){
            for (Cookie cookie:cookies){
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
