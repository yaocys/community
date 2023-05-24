package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.exception.VerifyException;
import com.example.community.service.LoginService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/17 0:59
 */
@Api(tags = "登录相关API")
@RestController
public class ApiLoginController implements CommunityConstant {
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Resource
    private UserService userService;
    @Resource
    private LoginService loginService;
    @Resource
    private Producer producer;
    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("注册")
    @PostMapping(path = "/register")
    public ApiResult<String> register(String username, String password, String email) {
        try {
            userService.register(username, password, email);
        } catch (VerifyException e) {
            return ApiResult.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
        return ApiResult.success("注册成功");
    }

    /**
     * 激活链接处理
     * <a href="http://localhost:8080/community/activation/101/code">...</a>
     **/
    @ApiOperation("注册账号激活")
    @GetMapping("/activation/{userId}/{code}")
    public ApiResult<String> activation(@PathVariable("userId") int userId, @PathVariable("code") String code) {
        try {
            userService.activation(userId, code);
        } catch (VerifyException e) {
            return ApiResult.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
        return ApiResult.success("注册成功");
    }


    @ApiOperation("生成验证码图片")
    @GetMapping("/captcha")
    public ApiResult<String> getCaptcha(HttpServletResponse response) {
        /*
          一个临时的验证码标识，用于验证码和用户的一一对应
          因为用户还没有登录，所以不能用登录凭证来标识
          临时标识同样返回给客户端存放在cookie中
         */
        String captchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("captchaOwner", captchaOwner);
        cookie.setMaxAge(60);
        cookie.setDomain("yaos.cc");
        cookie.setPath("/");
        response.addCookie(cookie);

        String text = producer.createText();
        /*
        向Redis中存入验证码
         */
        String redisKey = RedisKeyUtil.getKaptchaKey(captchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        BufferedImage image = producer.createImage(text);
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            return ApiResult.error("响应验证码失败");
        }
        return ApiResult.success("生成验证码成功");
    }

    /**
     * 注意这里的登陆成功重定向，登陆成功时是没有保存用户信息的，header上个人信息栏也不会显示
     * 重定向会刷新一次页面，再次加载用户信息
     */
    @ApiOperation("登录")
    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(String username, String password, String captcha, boolean rememberMe,
                                                HttpServletResponse response,
                                                @CookieValue(value = "captchaOwner", required = false) String captchaOwner) {
        Map<String, Object> userInfo;
        try {
            loginService.verifyCaptcha(captcha, captchaOwner);
            userInfo = userService.login(username, password, rememberMe, response);
        } catch (VerifyException e) {
            return ApiResult.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
        return ApiResult.success("登录成功", userInfo);
    }

    @ApiOperation("微信登录")
    @PostMapping("/wechatLogin")
    public ApiResult<Map<String,Object>> wechatLogin(String code,String nickname,String headerUrl) {
        Map<String,Object> res;
        try {
            res = loginService.wechatLogin(code,nickname,headerUrl);
        } catch (Exception e) {
            return ApiResult.error("Login fail");
        }
        return ApiResult.success("Login success", res);
    }

    @ApiOperation("注销登录")
    @GetMapping("/logout")
    public ApiResult<String> logout(@CookieValue("ticket") String ticket) {
        try {
            userService.logout(ticket);
            SecurityContextHolder.clearContext();
        } catch (VerifyException e) {
            return ApiResult.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
        return ApiResult.success("注销成功");
    }
}
