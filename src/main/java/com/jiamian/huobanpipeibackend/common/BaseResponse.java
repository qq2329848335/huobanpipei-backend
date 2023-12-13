/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/4
 * Time: 17:06
 */
package com.jiamian.huobanpipeibackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 * @author jiamian
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private T data;
    private String message;
    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data) {
        this(code,data,"","");
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }

    public BaseResponse(int code, T data, String message) {
        this(code,data,message,"");
    }

}
