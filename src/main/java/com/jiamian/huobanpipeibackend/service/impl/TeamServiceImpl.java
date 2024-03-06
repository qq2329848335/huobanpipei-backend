package com.jiamian.huobanpipeibackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.constant.UserConstant;
import com.jiamian.huobanpipeibackend.exception.BusinessException;
import com.jiamian.huobanpipeibackend.model.dto.TeamQuery;
import com.jiamian.huobanpipeibackend.model.entity.Team;
import com.jiamian.huobanpipeibackend.mapper.TeamMapper;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.model.entity.UserTeam;
import com.jiamian.huobanpipeibackend.model.enums.TeamStatusEnum;
import com.jiamian.huobanpipeibackend.model.request.TeamUpdateRequest;
import com.jiamian.huobanpipeibackend.model.vo.TeamUserVO;
import com.jiamian.huobanpipeibackend.model.vo.UserVO;
import com.jiamian.huobanpipeibackend.service.TeamService;
import com.jiamian.huobanpipeibackend.service.UserService;
import com.jiamian.huobanpipeibackend.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 队伍 服务实现类
 * </p>
 *
 * @author 加棉
 * @since 2023-12-23
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Resource
    TeamMapper teamMapper;

    @Resource
    UserService userService;

    @Resource
    UserTeamService userTeamService;

    @Override
    @Transactional()
    public boolean addTeam(Team team, long userId) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录？
        //3. 校验信息
        //  a. 队伍人数 >=1且<=20(必选)
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum <= 0 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, " 队伍人数 >=1且<=20");
        }
        //  b. 队名长度<=20(必选)
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() >= 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须设置队名,并且长度<=20");
        }
        //  c. 描述  长度<=512(可选)
        String description = team.getDescription();
        if (StringUtils.isNotBlank(name) && name.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述长度不能大于512个字符");
        }
        //  d. 过期时间 > 当前时间
        LocalDateTime expireTime = team.getExpireTime();
        LocalDateTime nowTime = LocalDateTime.now();
        if (expireTime != null && !(nowTime.isBefore(expireTime))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍过期时间要在当前时间之后");
        }
        //  e. status是否公开(int)，不传默认为0（公开）
        //  f. 如果status是加密状态，一定要有密码且密码长度<=32
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            String password = team.getPassword();
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密状态下必须设置密码,并且密码长度<=32");
            }
        }
        //  g. 校验用户最多创建5个队伍
        LambdaQueryWrapper<Team> lambdaQueryWrapper = new QueryWrapper<Team>().lambda();
        lambdaQueryWrapper.eq(Team::getUserId, userId);
        int count = this.count(lambdaQueryWrapper);
        if (count > UserConstant.MAX_CRATE_TEAM_NUMBER) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "每个用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍列表
        int insert = teamMapper.insert(team);
        Long teamId = team.getId();
        if (insert <= 0 || teamId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入队伍信息失败");
        }
        //5. 插入 用户-队伍关系到关系表中
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        boolean saveResult = userTeamService.save(userTeam);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入用户-队伍列表失败");
        }
        return true;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long id = teamQuery.getId();
        if (id != null && id > 0) {
            teamLambdaQueryWrapper.eq(Team::getId, id);
        }
        List<Long> idList = teamQuery.getIdList();
        /*if (idList != null) {
            for (int i = 0; i < idList.size(); i++) {
                Long id1 = idList.get(i);
                if (id1 != null && id1 > 0) {
                    if (i > 0) {
                        teamLambdaQueryWrapper.or();
                    }
                    teamLambdaQueryWrapper.eq(Team::getId, id1);
                }
            }
            idList.stream().forEach(id1 -> {

            });
        }*/
        if (idList!=null){
            teamLambdaQueryWrapper.in(Team::getId,idList);
        }
        String name = teamQuery.getName();
        if (StringUtils.isNotBlank(name)) {
            teamLambdaQueryWrapper.like(Team::getName, name);
        }
        String description = teamQuery.getDescription();
        if (StringUtils.isNotBlank(description)) {
            teamLambdaQueryWrapper.or().like(Team::getDescription, description);
        }
        Long userId1 = teamQuery.getUserId();
        if (userId1 != null && userId1 > 0) {
            teamLambdaQueryWrapper.eq(Team::getUserId, userId1);
        }
        //搜索关键词（同时对队伍名称和描述搜索）
        String searchText = teamQuery.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            teamLambdaQueryWrapper.and(eq ->eq.like(Team::getName, searchText).or().like(Team::getDescription, searchText));
            //写法二:teamLambdaQueryWrapper.and(qw ->{qw.like(Team::getName,searchText).or().like(Team::getDescription,searchText)});
        }
        Integer maxNum = teamQuery.getMaxNum();
        if (maxNum != null && maxNum >= 1) {
            teamLambdaQueryWrapper.eq(Team::getMaxNum, maxNum);
        }
        //是否忽略队伍状态
        Boolean isJudgeTeamState = teamQuery.getIsJudgeTeamState();
        if (isJudgeTeamState!=null&&isJudgeTeamState==true){
            //需要根据状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
            if (teamStatusEnum == null) {
                teamStatusEnum = TeamStatusEnum.PUBLIC;
            }
            //只有管理员才能搜索私有的队伍
            if (!userService.isAdmin(request) && teamStatusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NOT_AUTH);
            }
            teamLambdaQueryWrapper.eq(Team::getStatus, teamStatusEnum.getValue());
        }
        //不展示过期的队伍
        // expireTime is null or expireTime > now()
        teamLambdaQueryWrapper.ge(Team::getExpireTime, new Date());

        List<Team> teamList = this.list(teamLambdaQueryWrapper);

        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }

        //todo
        //已加入的用户数
        //已加入的用户信息


        return teamUserVOList;
    }


    @Override
    public boolean updateTeam(TeamUpdateRequest team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = team.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        User loginUser = userService.getLoginUser(request);
        // 只有管理员或者队伍的创建者可以修改
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(team.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(team, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean isTeamLeader(long teamId, Long userId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(Team::getUserId, userId).eq(Team::getId, teamId);
        Team team = teamMapper.selectOne(teamLambdaQueryWrapper);
        return team != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long teamId, HttpServletRequest request) {
        //1. 请求参数是否为空
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录
        userService.checkIsLogin(request);
        //3. 队伍是否存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //4. 是否是队长
        User loginUser = userService.getLoginUser(request);
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "您不是队长,无法解散队伍");
        }
        //5. 删除队伍&&删除所有加入队伍的关联信息
        int i = teamMapper.deleteById(teamId);
        if (i < 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        boolean result = userTeamService.remove(userTeamLambdaQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        return true;
    }
}
