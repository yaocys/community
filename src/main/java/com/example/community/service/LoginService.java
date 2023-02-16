package com.example.community.service;

import com.example.community.common.ApiResult;
import com.example.community.exception.VerifyException;
import com.example.community.util.CommunityUtil;
import com.example.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/17 1:21
 */
@Service
public class LoginService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

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
            kaptcha = redisTemplate.opsForValue().get(redisKey);
        } else {
            throw new VerifyException("验证码已过期，请重新刷新验证码");
        }

        if (StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(captcha)) {
            throw new VerifyException("验证码错误");
        }
    }
}
