package com.example.community.controller;

import com.example.community.common.ApiResult;
import com.example.community.service.DataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author yaosunique@gmail.com
 * 2023/3/13 20:20
 */
@Api(tags = "网站数据统计API")
@RestController
public class ApiDataController {
    @Resource
    private DataService dataService;

    @ApiOperation(value = "统计UV", notes = "独立访客数量")
    @PostMapping("/data/uv")
    public ApiResult<Long> getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start
            , @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        long uv = dataService.calculateUV(start, end);
        return ApiResult.success(uv);
    }

    @ApiOperation(value = "统计DAU", notes = "日活跃用户数量")
    @PostMapping("/data/dau")
    public ApiResult<Long> getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start
            , @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        long dau = dataService.calculateDAU(start, end);
        return ApiResult.success(dau);
    }
}
