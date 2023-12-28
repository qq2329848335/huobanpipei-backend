package com.jiamian.huobanpipeibackend.model.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 队伍更新请求体
 * @author 加棉
 * @since 2023-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 队伍id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;


    /**
     * 过期时间
     */
    // 前端  -> 后端时间处理。  请求体传 2021-10-26 15:12:49 ，不可传2021-10-26T15:12:49 ，请求头不可以
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime expireTime;


    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}
