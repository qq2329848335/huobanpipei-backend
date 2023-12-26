package com.jiamian.huobanpipeibackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户队伍关系
 * </p>
 *
 * @author 加棉
 * @since 2023-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_team")
public class UserTeam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户-队伍列表id
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;


}
