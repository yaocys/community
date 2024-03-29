package com.example.community.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.community.dao.LoginTicketMapper;
import com.example.community.dao.UserMapper;
import com.example.community.entity.LoginTicket;
import com.example.community.entity.User;
import com.example.community.entity.VO.UserVO;
import com.example.community.exception.VerifyException;
import com.example.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yao 2022/4/13
 */
@Service
public class UserService implements CommunityConstant {
    // TODO 更新密码还没做
    // 根据查询结果中的user_id替换为用户名
    @Autowired
    private UserMapper userMapper;
    /**
     * 邮件发送工具类
     */
    @Autowired
    private MailClient mailClient;
    /**
     * 模板引擎对象
     */
    @Autowired
    private TemplateEngine templateEngine;
    /*    @Autowired
        private LoginTicketMapper loginTicketMapper;*/
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 用于拼激活链接的域名
     */
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 用于拼激活链接的项目名
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        /*
        重写，从缓存中取用
         */
        // return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) user = initCache(id);
        return user;
    }

    /**
     * 校验注册信息
     */
    private void verifyRegisterInfo(String username, String password, String email) {
        // FIXME 这里要检查非法字符，不然存cookie要报错
        if (StringUtils.isBlank(username)) {
            throw new VerifyException("账号不能为空");
        }
        if (StringUtils.isBlank(password)) {
            throw new VerifyException("密码不能为空");
        }
        if (userMapper.selectByName(username) != null) {
            throw new VerifyException("账号已存在");
        }
        if (userMapper.selectByEmail(email) != null) {
            throw new VerifyException("邮箱已注册");
        }
    }

    public void register(String username, String password, String email) {
        verifyRegisterInfo(username, password, email);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(password + user.getSalt()));
        user.setType(0);
        user.setStatus(0);

        // TODO 这里的激活码并不需要持久化，后面可以放到Redis中并定时过期
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 准备一个Thymeleaf的Context对象
        Context context = new Context();
        // 把邮箱地址和激活链接交给thymeleaf，让它动态地放到邮件中
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // 生成html格式的内容
        String content = templateEngine.process("/mail/activation", context);
        // 发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);
    }

    // TODO 或许这里能封装一个统一的Result返回对象
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理与参数校验
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        // 验证用户名是否重复
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        // 生成一个UUID作为激活码并保存到数据库
        // TODO 这里的激活码并不需要持久化，后面可以放到Redis中并定时过期
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 准备一个Thymeleaf的Context对象
        Context context = new Context();
        // 把邮箱地址和激活链接交给thymeleaf，让它动态地放到邮件中
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // 生成html格式的内容
        String content = templateEngine.process("/mail/activation", context);
        // 发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * 注册激活
     */
    public int activation(int userId, String code) {
        /*
         * 激活结果的三种情况：
         * 0. 成功
         * 1. 重复激活
         * 2. 失败
         */
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            // status=1，已激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            // 提供的激活码和设置得激活码匹配，则激活成功
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 校验登录信息
     */
    private void verifyLoginInfo(String username, String password, User user) {
        if (StringUtils.isBlank(username)) {
            throw new VerifyException("账号不能为空");
        }
        /*
        考虑到通过微信登录的用户是没有密码的，而这里正好不允许密码为空
        于是就避免了有人用微信登录注册的账号+空密码去尝试登录网页
         */
        if (StringUtils.isBlank(password)) {
            throw new VerifyException("密码不能为空");
        }

        if (user == null) {
            throw new VerifyException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new VerifyException("账号未激活");
        }
        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            throw new VerifyException("密码错误");
        }
    }

    /**
     * 用户登录
     */
    public Map<String,Object> login(String username, String password, boolean rememberMe, HttpServletResponse response) {
        User user = userMapper.selectByName(username);
        verifyLoginInfo(username, password, user);

        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        /*
        生成登录凭证
         */
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // TODO 这里没有设置过期时间，只是标记状态不会导致redis占用越来越大吗
        // 对象会被序列化为字符串
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        /*
        保存登录凭证
        */
        Cookie cookie = new Cookie("ticket", loginTicket.getTicket());
        cookie.setPath(GLOBAL_PATH);
        cookie.setMaxAge(expiredSeconds);
        // cookie.setDomain("yaos.cc");
        /*
        在前端保存一份用户信息
        */
/*        Cookie userId= new Cookie("userId", user.getId()+"");
        Cookie headerUrl = new Cookie("headerUrl",user.getHeaderUrl());
        Cookie name = new Cookie("username",user.getUsername());
        userId.setPath(GLOBAL_PATH);
        headerUrl.setPath(GLOBAL_PATH);
        name.setPath(GLOBAL_PATH);
        userId.setMaxAge(expiredSeconds);
        headerUrl.setMaxAge(expiredSeconds);
        name.setMaxAge(expiredSeconds);

        response.addCookie(userId);
        response.addCookie(headerUrl);
        response.addCookie(name);*/
        response.addCookie(cookie);

        Map<String,Object> userInfo = new HashMap<>();
        userInfo.put("ticket",loginTicket.getTicket());// 只需要返回token而不需要整个对象
        userInfo.put("userId",user.getId());
        userInfo.put("headerUrl",user.getHeaderUrl());
        userInfo.put("username",user.getUsername());
        return userInfo;
    }

    /**
     * 注销登录
     *
     * @param ticket 存在cookie中的登录凭证
     */
    public void logout(String ticket) {
        // loginTicketMapper.updateStatus(ticket,1);
        // 修改登录凭证无效
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        // TODO 这里取出来改了再放回去不麻烦吗？存对象标记状态是为了有记录？直接删了不行吗
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        // 如果不存在这个登录用户，直接返回
        if (loginTicket == null) return;
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headUrl) {
        // FIXME 可能存在不一致
        int rows = userMapper.updateHeader(userId, headUrl);
        if (rows > 0) clearCache(userId);
        return rows;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    /**
     * 从缓存中取值
     * 私有主要是给自己使用
     *
     * @return 用户对象，用户信息
     */
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 当缓存中查不到值的时候去查数据库
     * 并初始化缓存
     *
     * @return 用户对象，用户信息
     */
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 数据变更时删除缓存
     */
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    /**
     * 获取用户权限字段用来放到securityContext中
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add((GrantedAuthority) () -> {
            switch (user.getType()) {
                case 1:
                    return AUTHORITY_ADMIN;
                case 2:
                    return AUTHORITY_MODERATOR;
                default:
                    return AUTHORITY_USER;
            }
        });
        return list;
    }
}
