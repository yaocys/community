package com.example.community.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/13 16:59
 */
@ApiModel(description = "统一API响应结果封装")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    @ApiModelProperty("响应状态码")
    private Integer code;
    @ApiModelProperty("响应信息")
    private String msg;

    @ApiModelProperty(notes = "结果集")
    private T data;

    public ApiResult(Integer code,String message){
        this.code = code;
        this.msg = message;
    }

    /**
     * 成功
     * 无参默认
     */
    public static <E> ApiResult<E> success(){
        return new ApiResult<>(ResultEnum.SUCCESS.getCode(),ResultEnum.SUCCESS.getMessage());
    }

    /**
     * 成功
     * 重载 指定消息
     */
    public static <E> ApiResult<E> success(String message){
        return new ApiResult<>(ResultEnum.SUCCESS.getCode(), message);
    }

    /**
     * 成功
     * 重载 指定返回数据
     */
    public static <E> ApiResult<E> success(E data){
        return new ApiResult<>(ResultEnum.SUCCESS.getCode(),ResultEnum.SUCCESS.getMessage(),data);
    }

    /**
     * 成功
     * 重载 指定消息和数据
     */
    public static <E> ApiResult<E> success(String message,E data){
        return new ApiResult<>(ResultEnum.SUCCESS.getCode(),message,data);
    }

    /**
     * 失败
     * 无参默认
     */
    public static <E> ApiResult<E> fail(){
        return new ApiResult<>(ResultEnum.FAIL.getCode(), ResultEnum.FAIL.getMessage());
    }

    /**
     * 失败
     * 指定 错误码和消息
     */
    public static <E> ApiResult<E> fail(Integer code,String message){
        return new ApiResult<>(code,message);
    }

}
