package com.jiamian.huobanpipeibackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.exception.BusinessException;
import com.jiamian.huobanpipeibackend.model.entity.Team;
import com.jiamian.huobanpipeibackend.mapper.TeamMapper;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.model.entity.UserTeam;
import com.jiamian.huobanpipeibackend.model.enums.TeamStatusEnum;
import com.jiamian.huobanpipeibackend.service.TeamService;
import com.jiamian.huobanpipeibackend.service.UserService;
import com.jiamian.huobanpipeibackend.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
    public boolean addTeam(Team team, HttpServletRequest request){
        //1. 请求参数是否为空？
        if (team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录？
        User loginUser = userService.getLoginUser(request);
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //3. 校验信息
        //  a. 队伍人数 >=1且<=20(必选)
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum<=0||maxNum>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR," 队伍人数 >=1且<=20");
        }
        //  b. 队名长度<=20(必选)
        String name = team.getName();
        if (StringUtils.isBlank(name)||name.length()>=20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"必须设置队名,并且长度<=20");
        }
        //  c. 描述  长度<=512(可选)
        String description = team.getDescription();
        if (StringUtils.isNotBlank(name)&&name.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述长度不能大于512个字符");
        }
        //  d. 过期时间 > 当前时间
        LocalDateTime expireTime = team.getExpireTime();
        LocalDateTime nowTime = LocalDateTime.now();
        if (expireTime!=null&&!(nowTime.isBefore(expireTime))){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍过期时间要在当前时间之后");
        }
        //  e. status是否公开(int)，不传默认为0（公开）
        //  f. 如果status是加密状态，一定要有密码且密码长度<=32
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            String password = team.getPassword();
            if (StringUtils.isBlank(password)||password.length()>32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密状态下必须设置密码,并且密码长度<=32");
            }
        }
        //  g. 校验用户最多创建5个队伍
        Long loginUserId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new QueryWrapper<UserTeam>().lambda();
        lambdaQueryWrapper.eq(UserTeam::getUserId,loginUserId.longValue());
        int count = userTeamService.count(lambdaQueryWrapper);
        if (count>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"每个用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍列表
        int insert = teamMapper.insert(team);
        Long teamId = team.getId();
        if (insert<=0||teamId==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"插入队伍信息失败");
        }
        //5. 插入 用户-队伍关系到关系表中
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUserId);
        userTeam.setTeamId(teamId);
        boolean saveResult = userTeamService.save(userTeam);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"插入用户-队伍列表失败");
        }
        return true;
    }
}
