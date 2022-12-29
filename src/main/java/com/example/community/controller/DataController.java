package com.example.community.controller;

import com.example.community.service.DataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * 处理统计网站数据的请求
 * @author yao 2022/12/1
 */
@Api(tags = "网站数据统计API")
@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    /**
     * 展示统计页面
     * @return 模板路径
     */
    @ApiOperation("展示统计页面")
    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    /**
     * 统计时间段内的UV
     */
    @ApiOperation("统计UV")
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start
            , @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model){
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);
        // 转发过去仍然是同一个请求，post请求
        return "forward:/data";
    }

    /**
     * 统计时间段内的DAU
     */
    @ApiOperation("统计DAU")
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start
            , @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model){
        long uv = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult",uv);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        // 转发过去仍然是同一个请求，post请求
        return "forward:/data";
    }
}
