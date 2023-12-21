package com.jiamian.huobanpipeibackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
@CrossOrigin(origins = {"http://localhost:5173"})
public class UserController {

    @Resource
    UserService userService;

    @Resource
    RedisTemplate redisTemplate;

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

    @GetMapping("/search/tagsAnd")
    public BaseResponse<List<User>> searchUserByTagsAnd(@RequestParam(value = "tagList",required=false) List<String> tagList,HttpServletRequest request) {
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"搜索标签不能为空");
        }
        //校验是否已登录
        //userService.checkIsLogin(request);
        List<User> userList = userService.searchUserByTagAnd(tagList);
        return ResultUtil.success(userList);
    }

    @GetMapping("/search/tagsOr")
    public BaseResponse<List<User>> searchUserByTagsOr(@RequestParam(value = "tagList",required=false) List<String> tagList,HttpServletRequest request) {
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"搜索标签不能为空");
        }
        //判断是否已登录
        //userService.checkIsLogin(request);

        List<User> userList = userService.searchUserByTagsOr(tagList);
        return ResultUtil.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> userRecommend(long pageSize,long pageNum,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        //从缓存中取
        String redisKey = String.format("yupao:user:recommend:%s",loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        if (userPage==null){
            //无缓存,查数据库
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userPage = userService.page(new Page<User>(pageNum, pageSize), userQueryWrapper);
            //写入缓存,10s过期
            try {
                valueOperations.set(redisKey,userPage,60000, TimeUnit.MILLISECONDS);
            } catch (Exception e){
                log.error("redis set key error",e);
            }
        }
        //脱敏
        List<User> records = userPage.getRecords();
        List<User> userList = records.stream().map(user ->
            userService.getSafetyUser(user)
        ).collect(Collectors.toList());
        userPage.setRecords(userList);

        return ResultUtil.success(userPage);
    }



    @GetMapping("/userSearch")
    public BaseResponse<List<User>> userSearch(String username, HttpServletRequest request) {
        //校验是否已登录
        userService.checkIsLogin(request);

        //校验权限
        if (!userService.isAdmin(request)){
            log.info("/userSearch  无管理员权限");
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }

        List<User> userList = userService.userSearch(username);
        return ResultUtil.success(userList);
    }

    @DeleteMapping("/userDelete")
    public BaseResponse<Boolean> userDelete(Long id, HttpServletRequest request) {
        //校验是否已登录
        userService.checkIsLogin(request);
        //校验权限
        if (!userService.isAdmin(request)){
            log.info("/userDelete  无管理员权限");
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }

        if (id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtil.success(result);
    }

    @GetMapping("/getCurrentUser")
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

    @PostMapping("/update")
    public BaseResponse<Integer> userUpdate(@RequestBody User user,HttpServletRequest request) {
        //1.校验参数
        if (user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有传要修改的用户");
        }
        return ResultUtil.success(userService.userUpdate(user,request));

    }






}



