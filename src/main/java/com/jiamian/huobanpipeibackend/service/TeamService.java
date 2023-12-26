package com.jiamian.huobanpipeibackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiamian.huobanpipeibackend.model.dto.TeamQuery;
import com.jiamian.huobanpipeibackend.model.entity.Team;
import com.jiamian.huobanpipeibackend.model.vo.TeamUserVO;

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
     * @param team
     * @param request
     * @return
     */
    boolean addTeam(Team team, HttpServletRequest request);

    /**
     * 查找队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request);
}
