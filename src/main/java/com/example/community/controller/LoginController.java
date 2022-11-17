package com.example.community.controller;

import com.example.community.entity.User;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author yaosu
 */
@Controller
public class LoginController implements CommunityConstant {

    /**
     * 日志对象
     */
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @Value("${server.servlet.context-path")
    private String contextPath;

    /**
     * 返回注册页面
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 注册方法
     * @param user 要注册的用户对象
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            // 注册成功，跳转到结果页面
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            // 注册失败，填入提示信息并刷新回注册页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 激活链接处理
     * <a href="http://localhost:8080/community/activation/101/code">...</a>
     * @param userId 用户ID
     * @param code 激活码
     */
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 生成验证码图片
     * @param session 用于存放
     */
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        // 获取四位字符串
        String text = producer.createText();
        // 生成图片
        BufferedImage image = producer.createImage(text);
        session.setAttribute("kaptcha",text);
        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败"+e.getMessage());
        }

    }

    /**
     * 注意这里的登陆成功重定向，登陆成功时是没有保存用户信息的，header上个人信息栏也不会显示
     * 重定向会刷新一次页面，再次加载用户信息
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param rememberme “记住我”
     * @param model 存视图数据
     * @param session 存验证码
     * @param response 返回验证码图片
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,Model model,HttpSession session,HttpServletResponse response){
        // 判断验证码
        String kaptcha = (String) session.getAttribute("kaptcha");

        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }
        // 是否勾选“记住我”->保存时间不同
        int expiredSeconds = rememberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        // 检查账号、密码
        Map<String,Object> map = userService.login(username,password,expiredSeconds);

        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            // 设置生效范围
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }

    /**
     * 注意这里也是重定向刷新页面
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}
