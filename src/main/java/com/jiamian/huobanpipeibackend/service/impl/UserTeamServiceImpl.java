package com.jiamian.huobanpipeibackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.constant.UserConstant;
import com.jiamian.huobanpipeibackend.exception.BusinessException;
import com.jiamian.huobanpipeibackend.mapper.TeamMapper;
import com.jiamian.huobanpipeibackend.model.entity.Team;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.model.entity.UserTeam;
import com.jiamian.huobanpipeibackend.mapper.UserTeamMapper;
import com.jiamian.huobanpipeibackend.model.enums.TeamStatusEnum;
import com.jiamian.huobanpipeibackend.model.request.UserTeamAddRequest;
import com.jiamian.huobanpipeibackend.service.TeamService;
import com.jiamian.huobanpipeibackend.service.UserService;
import com.jiamian.huobanpipeibackend.service.UserTeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户队伍关系 服务实现类
 * </p>
 *
 * @author 加棉
 * @since 2023-12-23
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements UserTeamService {

    @Resource
    UserTeamMapper userTeamMapper;

    @Resource
    TeamMapper teamMapper;

    @Resource
    UserService userService;


    @Override
    public boolean addUserTeam(UserTeamAddRequest userTeamAddRequest, HttpServletRequest request) {
        //1. 请求参数是否为空
        if (userTeamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = Long.parseLong(userTeamAddRequest.getTeamId());
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要加入的队伍");
        }
        //2. 队伍是否存在？
//        Team team = teamMapper.getById(teamId);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //3. 队伍是否可加入（其他人、未满、未过期）
        //  a. 用户是否已经加入（其他人）
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long loginUserId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId,loginUserId).eq(UserTeam::getTeamId,teamId);
        UserTeam one = this.getOne(userTeamLambdaQueryWrapper);
        if (one!=null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已在队伍中,不需要再加入");
        }
        //  b. 队伍是否达到最大人数
        LambdaQueryWrapper<UserTeam> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        int count = this.count(teamLambdaQueryWrapper);
        if (count >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已达到最大人数");
        }
        //  c. 队伍是否过期
        LocalDateTime expireTime = team.getExpireTime();
        if (LocalDateTime.now().isAfter(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该队伍已过期");
        }
        //4. 用户加入的队伍个数是否达到上限？
        int count1 = this.countByUserId(loginUserId);
        if (count1>= UserConstant.MAX_JOIN_TEAM_NUMBER){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "可加入队伍的数量已达上限");
        }
        //5.如果队伍是加密的,那么密码是否正确?
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            String inputPassword = userTeamAddRequest.getPassword();
            String password = team.getPassword();
            if (password!=null&&!password.equals(inputPassword)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        //6. 插入到user_team列表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUserId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(LocalDateTime.now());
        userTeam.setUpdateTime(LocalDateTime.now());
        boolean save = this.save(userTeam);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加队伍失败,插入user->team表时出现问题");
        }
        return true;
    }

    @Override
    public int countByTeamId(Long teamId) {
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        return this.count(userTeamLambdaQueryWrapper);
    }

    @Override
    public int countByUserId(Long loginUserId) {
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, loginUserId);
        return this.count(userTeamLambdaQueryWrapper);
    }

    @Override
    @Transactional()
    public boolean deleteUserTeam(Long teamId, Long userId) {
        //1. 请求参数是否为空
        if (teamId == null||teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId==null||userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 验证队伍是否存在
        Team team = teamMapper.selectById(teamId);
        if (team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //3. 验证我是否已加入队伍
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId,userId).eq(UserTeam::getTeamId,teamId);
        UserTeam userTeam = userTeamMapper.selectOne(userTeamLambdaQueryWrapper);
        if (userTeam==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"您未加入该队伍,无法退出");
        }
        //4. 如果队伍
        //  a.  只剩一人，队伍解散
        int numberOfUsersInTheTeam = this.countByTeamId(teamId);
        if (numberOfUsersInTheTeam==1){
            userTeamMapper.deleteById(userTeam.getId());
            teamMapper.deleteById(teamId);
            return true;
        }
        //  b.  还有其他人
        //    ⅰ.  如果是队长退出队伍，权限转移给第二早加入的用户 —— 先来后到
        //取更新时间最小的那条数据
        boolean isTeamLeader = this.isTeamLeader(teamId,userId);
        if (isTeamLeader){
            userTeamMapper.deleteById(userTeam.getId());
//            int i = 1 / 0;
            LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            //根据更新时间从小到大排序
            userTeamLambdaQueryWrapper1.eq(UserTeam::getTeamId,teamId).orderByAsc(UserTeam::getUpdateTime);
            List<UserTeam> userTeamList = this.list(userTeamLambdaQueryWrapper1);
            Long newTeamLeaderId = userTeamList.get(0).getUserId();
            //修改队伍的队长 为上面这个用户
            team.setUserId(newTeamLeaderId);
            teamMapper.updateById(team);
            return true;
        }
        //   	 ⅱ.  非队长，自己退出队伍
        userTeamMapper.deleteById(userTeam.getId());
        return true;
    }

    @Override
    public boolean isTeamLeader(long teamId, Long userId) {
        if (teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId == null||userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(Team::getUserId,userId).eq(Team::getId,teamId);
        Team team = teamMapper.selectOne(teamLambdaQueryWrapper);
        return team != null;
    }
}
