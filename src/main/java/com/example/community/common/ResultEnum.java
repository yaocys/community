package com.example.community.common;

import lombok.Data;

/**
 * @author yaocy yaosunique@gmail.com
 * 2023/2/13 17:13
 */
public enum ResultEnum {

    SUCCESS(200,"System Success"),
    FAIL(400, "Request Wrong"),
    ERROR(500,"System Error");

    private Integer code;
    private String message;

    public void setCode(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private ResultEnum(int code,String message){
        this.code = code;
        this.message = message;
    }
}
