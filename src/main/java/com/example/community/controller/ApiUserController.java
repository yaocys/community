package com.example.community.controller;

import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.HostHolder;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/17 0:55
 */
@Api(tags = "用户API")
@RestController
@RequestMapping("/user")
public class ApiUserController implements CommunityConstant {
    @Resource
    private UserService userService;
    @Resource
    private HostHolder hostHolder;
}
