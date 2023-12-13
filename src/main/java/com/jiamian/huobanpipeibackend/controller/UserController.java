package com.jiamian.huobanpipeibackend.controller;


import com.jiamian.huobanpipeibackend.common.BaseResponse;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.common.ResultUtil;
import com.jiamian.huobanpipeibackend.exception.BusinessException;
import com.jiamian.huobanpipeibackend.request.UserRegisterRequest;
import com.jiamian.huobanpipeibackend.service.UserService;
import com.jiamian.huobanpipeibackend.constant.UserConstant;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.request.UserLoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author 加棉
 * @since 2023-12-01
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/userRegister")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            //return ResultUtil.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();


        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtil.success(result);
    }

    @PostMapping("/userLogin")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            //return ResultUtil.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();


        User user = userService.doLogin(userAccount, userPassword, request);
        return ResultUtil.success(user);
    }

    @PostMapping("/userLogout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtil.success(result);
    }



    @GetMapping("/userSearch")
    public BaseResponse<List<User>> userSearch(String username, HttpServletRequest request) {
        //判断是否已登录
        boolean login = isLogin(request);
        if (!login){
            //未登录
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //校验权限
        if (!isAdmin(request)){
            log.info("/userSearch  无管理员权限");
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }

        List<User> userList = userService.userSearch(username);
        return ResultUtil.success(userList);
    }

    @DeleteMapping("/userDelete")
    public BaseResponse<Boolean> userDelete(Long id, HttpServletRequest request) {
        //判断是否已登录
        boolean login = isLogin(request);
        if (!login){
            //未登录
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //校验权限
        if (!isAdmin(request)){
            log.info("/userDelete  无管理员权限");
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }

        if (id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtil.success(result);
    }

    @PostMapping("/getCurrentUser")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //防止   (存入登陆态的时候 -- 现在)这段时间用户信息有更改.
        //我们需要通过数据库得到最新的信息再返回给前端
        Long id = currentUser.getId();
        // todo 校验用户是否合法
        User user = userService.getById(id);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtil.success(safetyUser);
    }


    /**
     * 判断是否已登录
     * @param request
     * @return true --已登录， false --未登录
     */
    private boolean isLogin(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user==null){
            //未登录
            return false;
        }
        return true;
    }


    /**
     * 校验是否是管理员
     * @param request
     * @return true --是 ，false --不是
     */
    private boolean isAdmin(HttpServletRequest request){
        //校验权限
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user==null || UserConstant.MANAGER_ROLE.equals(user.getUserRole())){
            log.info("未登录或无管理员权限");
            return false;
        }
        return true;
    }
}



