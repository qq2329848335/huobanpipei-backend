package com.jiamian.huobanpipeibackend.controller;


import com.jiamian.huobanpipeibackend.common.BaseResponse;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.common.ResultUtil;
import com.jiamian.huobanpipeibackend.exception.BusinessException;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.model.request.UserTeamAddRequest;
import com.jiamian.huobanpipeibackend.service.UserService;
import com.jiamian.huobanpipeibackend.service.UserTeamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户队伍关系 前端控制器
 * </p>
 *
 * @author 加棉
 * @since 2023-12-23
 */
@RestController
@RequestMapping("/userteam")
public class UserTeamController {
    @Resource
    UserTeamService userTeamService;

    @Resource
    UserService userService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addUserTeam(@RequestBody UserTeamAddRequest userTeamAddRequest, HttpServletRequest request){
        if (userTeamAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userTeamService.addUserTeam(userTeamAddRequest,request);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据插入异常");
        }
        return ResultUtil.success(true);
    }

    @PutMapping("/delete")
    public BaseResponse<Boolean> deleteUserTeam(Long teamId,HttpServletRequest request){
        if (teamId == null||teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入正确的队伍id");
        }
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = userTeamService.deleteUserTeam(teamId,loginUser.getId());
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtil.success(true);
    }

}
