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
     *
     * @param userTeamAddRequest
     * @param request
     * @return
     */
    boolean addUserTeam(UserTeamAddRequest userTeamAddRequest, HttpServletRequest request);

    /**
     * 统计加入该队伍的人数
     * @param teamId
     * @return
     */
    int countByTeamId(Long teamId);

    /**
     * 统计用户加入的队伍数量
     *
     * @param loginUserId
     * @return
     */
    int countByUserId(Long loginUserId);

    /**
     * 用户退出队伍
     *
     * @param teamId 队伍id
     * @param userId 用户id
     * @return
     */
    boolean deleteUserTeam(Long teamId, Long userId);

    /**
     * 验证用户是否是队长
     * @param teamId
     * @param userId
     * @return
     */
    boolean isTeamLeader(long teamId, Long userId);
}
