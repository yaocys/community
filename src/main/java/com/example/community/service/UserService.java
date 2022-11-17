package com.example.community.service;

import com.example.community.dao.LoginTicketMapper;
import com.example.community.dao.UserMapper;
import com.example.community.entity.LoginTicket;
import com.example.community.entity.User;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author yao 2022/4/13
 */
@Service
public class UserService implements CommunityConstant {
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
    @Autowired
    private LoginTicketMapper loginTicketMapper;

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

    public User findUserById(int id){
        return userMapper.selectById(id);
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
        // 生成html格式地内容
        String content = templateEngine.process("/mail/activation", context);
        // 发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * 注册激活
     */
    public int activation(int userId, String code) {
        /**
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
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,long expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("password","密码不能为空");
            return map;
        }
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg","账号未激活");
        }
        // 验证密码
        password = CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        // 这里用session的SessionID不行吗？
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        // 返回给客户端的凭证
        map.put("ticket",loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
        // 修改登录凭证无效
    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId,String headUrl){
        return userMapper.updateHeader(userId,headUrl);
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

}
