/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/4
 * Time: 18:14
 */
package com.jiamian.huobanpipeibackend.exception;

import com.jiamian.huobanpipeibackend.common.ErrorCode;

public class BusinessException extends RuntimeException {
    private final int code;
    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode,String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
