package com.jiamian.huobanpipeibackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiamian.huobanpipeibackend.model.entity.UserTeam;
import com.jiamian.huobanpipeibackend.model.request.UserTeamAddRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户队伍关系 服务类
 * </p>
 *
 * @author 加棉
 * @since 2023-12-23
 */
public interface UserTeamService extends IService<UserTeam> {

    /**
     * 用户加入队伍
     * @return
     * @param userTeamAddRequest
     * @param request
     */
    boolean addUserTeam(UserTeamAddRequest userTeamAddRequest, HttpServletRequest request);

    /**
     * 统计用户加入的队伍数量
     * @param loginUserId
     * @return
     */
    int countByUserId(Long loginUserId);
}
