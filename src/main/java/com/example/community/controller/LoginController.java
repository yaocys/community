package com.example.community.controller;

import com.example.community.entity.User;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yaosu
 */
@Api(tags = "登录相关API")
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
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 返回注册页面
     */
    @ApiOperation("注册页面")
    @GetMapping(path = "/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @ApiOperation("登录页面")
    @GetMapping(path = "/login")
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 注册方法
     *
     * @param user 要注册的用户对象
     */
    @ApiOperation("注册")
    @PostMapping(path = "/register")
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
     *
     * @param userId 用户ID
     * @param code   激活码
     */
    @ApiOperation("注册账号激活")
    @GetMapping(path = "/activation/{userId}/{code}")
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
     */
    @ApiOperation("生成验证码图片")
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 获取四位字符串
        String text = producer.createText();

        /*
          一个临时的验证码标识，用于验证码和用户的一一对应
          因为用户还没有登录，所以不能用登录凭证来标识
          临时标识同样返回给客户端存放在cookie中
         */
        String katpchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", katpchaOwner);
        // TODO 验证码有效期一分钟，那么前端应该有相应的提示逻辑提示“验证码已过期”
        cookie.setMaxAge(60);
        // 设置cookie的生效范围
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        /*
        向Redis中存入验证码
         */
        String redisKey = RedisKeyUtil.getKaptchaKey(katpchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        /*
        被优化的代码，存在redis而不是session
         */
        // session.setAttribute("kaptcha",text);

        /*
        生成图片
        将图片输出给浏览器
         */
        BufferedImage image = producer.createImage(text);
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }

    /**
     * 注意这里的登陆成功重定向，登陆成功时是没有保存用户信息的，header上个人信息栏也不会显示
     * 重定向会刷新一次页面，再次加载用户信息
     *
     * @param username   用户名
     * @param password   密码
     * @param code       验证码
     * @param rememberme “记住我”
     * @param model      存视图数据
     * @param response   返回验证码图片
     */
    @ApiOperation("登录")
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*, HttpSession session*/, HttpServletResponse response,
                        @CookieValue(value = "kaptchaOwner", required = false) String kaptchaOwner) {
        /*
        判断验证码
         */
        String kaptcha;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        } else {
            model.addAttribute("codeMsg", "验证码已过期，请重新刷新验证码");
            return "/site/login";
        }

        /*
        被优化的代码，redis替代session
         */
        // String kaptcha = (String) session.getAttribute("kaptcha");

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }
        // 是否勾选“记住我”->保存时间不同
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        // 检查账号、密码
        Map<String, Object> map = userService.login(username, password, expiredSeconds);

        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            // 设置生效范围
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    /**
     * 注意这里也是重定向刷新页面
     */
    @ApiOperation("注销")
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
