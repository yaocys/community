package com.example.community.service;

import com.alibaba.fastjson.JSONObject;
import com.example.community.dao.UserMapper;
import com.example.community.entity.LoginTicket;
import com.example.community.entity.User;
import com.example.community.exception.VerifyException;
import com.example.community.util.CommunityUtil;
import com.example.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Date;

import static com.example.community.util.CommunityConstant.DEFAULT_EXPIRED_SECONDS;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/17 1:21
 */
@Service
public class LoginService {
    @Resource
    private RestTemplate restTemplate;
    @Value("${wechat.appId}")
    private String appId;
    @Value("${wechat.appSecret}")
    private String appSecret;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserMapper userMapper;

    /**
     * 校验验证码
     */
    public void verifyCaptcha(String captcha, String captchaOwner) {
        if(StringUtils.isBlank(captcha)){
            throw new VerifyException("验证码不能为空");
        }
        String kaptcha;
        if (StringUtils.isNotBlank(captchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(captchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        } else {
            throw new VerifyException("验证码已过期，请重新刷新验证码");
        }

        if (StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(captcha)) {
            throw new VerifyException("验证码错误");
        }
    }

    /**
     * 微信登陆
     * @param code 微信提供的
     * @return 登录凭证token
     */
    public String wechatLogin(String code,String nickname,String headerUrl){
        String WECHAT_LOGIN_API = "https://api.weixin.qq.com/sns/jscode2session?appid=";
        String url = WECHAT_LOGIN_API +appId+ "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        LoginTicket loginTicket = new LoginTicket();

        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();
            JSONObject json = JSONObject.parseObject(responseBody);

            // 解析响应结果，获取用户唯一标识 openid 和会话密钥 session_key
            String openid = json.getString("openid");
            String sessionKey = json.getString("session_key");

            // 创建一个新用户
            User user = new User();
            user.setUsername(nickname);
            user.setHeaderUrl(headerUrl);
            user.setType(0);
            user.setStatus(1);
            user.setCreateTime(new Date());
            userMapper.insertUser(user);

            // 生成一份登录凭证
            loginTicket.setUserId(user.getId());
            loginTicket.setTicket(CommunityUtil.generateUUID());
            loginTicket.setStatus(0);
            loginTicket.setExpired(new Date(System.currentTimeMillis() + DEFAULT_EXPIRED_SECONDS * 1000L));
            loginTicket.setOpenId(openid);
            loginTicket.setSessionKey(sessionKey);

            String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
            redisTemplate.opsForValue().set(redisKey, loginTicket);
        }
        return loginTicket.getTicket();
    }
}
