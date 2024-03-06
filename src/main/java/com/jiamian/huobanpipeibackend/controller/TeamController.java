package com.jiamian.huobanpipeibackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiamian.huobanpipeibackend.common.BaseResponse;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.common.ResultUtil;
import com.jiamian.huobanpipeibackend.exception.BusinessException;
import com.jiamian.huobanpipeibackend.model.dto.TeamQuery;
import com.jiamian.huobanpipeibackend.model.entity.Team;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.model.entity.UserTeam;
import com.jiamian.huobanpipeibackend.model.request.TeamAddRequest;
import com.jiamian.huobanpipeibackend.model.request.TeamUpdateRequest;
import com.jiamian.huobanpipeibackend.model.vo.TeamUserVO;
import com.jiamian.huobanpipeibackend.service.TeamService;
import com.jiamian.huobanpipeibackend.service.UserService;
import com.jiamian.huobanpipeibackend.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 队伍 前端控制器
 * </p>
 *
 * @author 加棉
 * @since 2023-12-23
 */
@Slf4j
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173"})
public class TeamController {

    @Resource
    TeamService teamService;

    @Resource
    UserTeamService userTeamService;

    @Resource
    UserService userService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录？
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        //默认当前登录用户是队长
        Long userId = loginUser.getId();
        team.setUserId(userId);
        boolean save = teamService.addTeam(team,userId);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入异常");
        }
        return ResultUtil.success(true);
    }


    /*@PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(long id){
        if (id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"id必须大于0");
        }
        boolean result = teamService.removeById(id);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除数据异常");
        }
        return ResultUtil.success(true);
    }*/

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Long teamId, HttpServletRequest request) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.deleteTeam(teamId, request);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散队伍失败");
        }
        return ResultUtil.success(true);
    }


    @PostMapping("/update")
    public BaseResponse<Boolean> updateteam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.updateTeam(teamUpdateRequest, request);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新数据异常");
        }
        return ResultUtil.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        return ResultUtil.success(team);
    }


    /*@GetMapping("/list")
    public BaseResponse<List<Team>> selectTeam(@RequestBody TeamQuery teamQuery){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(team);
        List<Team> teamList = teamService.list(teamQueryWrapper);
        return ResultUtil.success(teamList);
    }*/

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> selectTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, request);
        return ResultUtil.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> selectTeamByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(team);
        Page<Team> page = teamService.page(new Page<Team>(teamQuery.getPageNum(), teamQuery.getPageSize()), teamQueryWrapper);
        return ResultUtil.success(page);
    }

    @PostMapping("/list/update")
    public BaseResponse<Boolean> updateTeam(TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        if (teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.updateTeam(teamUpdateRequest, request);
        return ResultUtil.success(true);
    }

    /**
     * 获取当前用户已加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/user/team/join")
    public BaseResponse<List<TeamUserVO>> getUserJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //1.先从user_team表中查询出 用户加入的记录
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId,loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
        //2.从步骤1的结果中提取队伍id,再根据队伍id查队伍的信息,返回
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        //设置查询时不需要判断队伍状态(即公开,私有,加密的都可查到)
        teamQuery.setIsJudgeTeamState(false);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, request);
        return ResultUtil.success(teamList);
    }

    /**
     * 获取当前用户创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/user/team/create")
    public BaseResponse<List<TeamUserVO>> getUserCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        //设置查询时不需要判断队伍状态(即公开,私有,加密的都可查到)
        teamQuery.setIsJudgeTeamState(false);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, request);
        return ResultUtil.success(teamList);
    }
}
