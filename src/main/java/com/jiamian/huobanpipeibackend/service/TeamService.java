package com.jiamian.huobanpipeibackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiamian.huobanpipeibackend.model.dto.TeamQuery;
import com.jiamian.huobanpipeibackend.model.entity.Team;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.model.request.TeamUpdateRequest;
import com.jiamian.huobanpipeibackend.model.vo.TeamUserVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 队伍 服务类
 * </p>
 *
 * @author 加棉
 * @since 2023-12-23
 */
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     *
     * @param team
     * @param userId
     * @return
     */
    @Transactional()
    boolean addTeam(Team team, long userId);

    /**
     * 查找队伍
     *
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request);


    /**
     * 修改队伍信息
     *
     * @param team
     * @param request
     * @return
     */
    boolean updateTeam(TeamUpdateRequest team, HttpServletRequest request);


    /**
     * 判断用户是否是队长
     *
     * @param userId
     * @return
     */
    boolean isTeamLeader(long teamId, Long userId);

    /**
     * 解散队伍
     *
     * @param teamId
     * @param request
     * @return
     */
    boolean deleteTeam(Long teamId, HttpServletRequest request);
}
