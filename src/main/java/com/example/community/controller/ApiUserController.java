package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.entity.User;
import com.example.community.entity.VO.ProfileVO;
import com.example.community.service.FollowService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/17 0:55
 */
@Api(tags = "用户API")
@RestController
@RequestMapping("/user")
public class ApiUserController implements CommunityConstant {
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Resource
    private UserService userService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private LikeService likeService;
    @Resource
    private FollowService followService;


    @ApiOperation(value = "上传头像", notes = "上传必须是POST请求")
    @PostMapping(path = "/upload")
    public ApiResult<String> uploadHeader(MultipartFile headerImage) {
        if (headerImage == null) return ApiResult.fail("未选择图片");

        // 获取原始文件名
        String fileName = headerImage.getOriginalFilename();
        // 保留文件后缀，断言文件名不为空
        assert fileName != null;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) return ApiResult.fail("文件格式不正确");
        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;

        // 确定文件存放路径，保存文件
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("上传文件失败，服务器发生异常：" + e);
        }
        //更新当前用户头像的web访问路径
        // ……/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return ApiResult.success("头像上传成功");
    }

    @ApiOperation("获取头像")
    @GetMapping(path = "/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                // 新语法，自动关闭
                OutputStream outputStream = response.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(fileName);
        ) {
            byte[] buffer = new byte[1024];
            int b;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取头像文件失败：" + e);
        }
    }

    @ApiOperation("查看指定用户的个人主页")
    @GetMapping("/profile/{userId}")
    public ApiResult<ProfileVO> getProfilePage(@PathVariable("userId") int userId) {
        User user = userService.findUserById(userId);
        ProfileVO profileVO = new ProfileVO();
        if (user == null) return ApiResult.fail("该用户不存在");
        profileVO.setUser(user);

        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        profileVO.setLikeCount(likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        profileVO.setFolloweeCount(followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        profileVO.setFollowerCount(followerCount);
        // 当前登录用户是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null)
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);

        profileVO.setHasFollowed(hasFollowed);

        return ApiResult.success(profileVO);
    }
}
