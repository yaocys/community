package com.example.community.controller;

import com.example.community.annotation.LoginRequired;
import com.example.community.entity.User;
import com.example.community.service.UserService;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author yao 2022/5/5
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage==null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        // 保留文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        // 生成随机文件名
        fileName = CommunityUtil.generateUUID()+suffix;
        // 确定文件存放路径
        File dest = new File(uploadPath+"/"+fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("上传文件失败，服务器发生异常："+e);
        }
        //更新当前用户头像的web访问路径
        // ……/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    /**
     * 获取头像
     * @param fileName
     * @param response
     */
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // 服务器存放路径
        fileName = uploadPath+"/"+fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/"+suffix);
        try(
                // 新语法，自动关闭
                OutputStream outputStream = response.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(fileName);
                ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer))!=-1){
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取头像文件失败："+e);
        }
    }
}
