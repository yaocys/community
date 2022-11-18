package com.example.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 通用工具类
 * @author yaosu
 */
public class CommunityUtil {

    /**
     * 生成随机字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 对加盐的字符串生成对应的MD5字符串
     * @param key 对加盐的字符串
     * @return 生成的MD5字符串
     */
    public static String md5(String key) {
        if (StringUtils.isBlank(key))return null;
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 转json格式字符串
     * @param code 相应码
     * @param msg 消息
     * @param map 数据
     * @return json格式字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    /**
     * 重载
     */
    public static String getJSONString(int code, String msg) {
        return getJSONString(code,msg,null);
    }

    /**
     * 重载
     */
    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }
}
