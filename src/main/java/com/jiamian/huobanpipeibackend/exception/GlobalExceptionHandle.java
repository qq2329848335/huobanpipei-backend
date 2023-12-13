/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/4
 * Time: 19:02
 */
package com.jiamian.huobanpipeibackend.exception;

import com.jiamian.huobanpipeibackend.common.BaseResponse;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.common.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author jiamian
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandle(BusinessException e){
        log.error("BusinessException: "+e.getMessage(),e);
        return ResultUtil.error(e.getCode(),e.getMessage(),e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse businessExceptionHandle(RuntimeException e){
        log.error("RuntimeException: "+e.getMessage(),e);
        return ResultUtil.error(ErrorCode.SYSTEM_ERROR);
    }
}
