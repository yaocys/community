package com.example.community.exception;

import com.example.community.common.ResultEnum;

/**
 * 参数校验异常
 * @author yaocy yaosunique@gmail.com
 * 2023/2/17 1:25
 */
public class VerifyException extends RuntimeException{

    private Integer code;

    public Integer getCode(){
        return this.code;
    }

    public void setCode(Integer code){
        this.code = code;
    }

    public VerifyException(){
        super(ResultEnum.FAIL.getMessage());
        this.code = ResultEnum.FAIL.getCode();
        this.setCode(ResultEnum.FAIL.getCode());
    }

    public VerifyException(String message) {
        super(message);
        this.code = ResultEnum.FAIL.getCode();
        this.setCode(ResultEnum.FAIL.getCode());
    }
}
