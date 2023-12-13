/**
 * Created by IntelliJ IDEA.
 * User: 加棉
 * Date: 2023/12/4
 * Time: 17:11
 */
package com.jiamian.huobanpipeibackend.common;

public class ResultUtil {
    /**
     * 成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<T>(200,data,"ok");
    }


    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse(errorCode);
    }

    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code,"",message,description);
    }
}
