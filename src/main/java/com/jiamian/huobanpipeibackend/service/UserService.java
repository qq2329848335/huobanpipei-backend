package com.jiamian.huobanpipeibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiamian.huobanpipeibackend.common.BaseResponse;
import com.jiamian.huobanpipeibackend.model.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author 加棉
 * @since 2023-12-01
 */
public interface UserService extends IService<User> {


    /**
     * 用户注册
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkCode 验证码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkCode);

    /**
     * 用户登录
     * @param userAccount 账号
     * @param userPassword 密码
     * @return 脱敏后的用户信息
     */
    User doLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销/退出登录
     * @param request
     */
    int userLogout(HttpServletRequest request);

    /**
     * 用户模糊搜索
     * @param username 用户名
     * @return 脱敏后用户集合
     */
    List<User> userSearch(String username);


    /**
     * 用户脱敏操作
     * @param user
     * @return 脱敏后的用户
     */
    User getSafetyUser(User user);



    /**
     * 根据标签搜索用户(包含所有搜索标签)
     * @param tagList 要求用户包含的标签列表
     * @return 脱敏后用户集合
     */
    List<User> searchUserByTagAnd(List<String> tagList);


    /**
     * 根据标签搜索用户(只要包含一个标签就行)
     * @param tagList 要求用户包含的标签列表
     * @return 脱敏后用户集合
     */
    List<User> searchUserByTagsOr(List<String> tagList);

    /**
     * 修改用户信息
     * @param user 目标用户信息
     * @param request 用于获取当前登录的用户,以便判断是否有权限
     * @return 修改后的用户信息
     */
    int userUpdate(User user, HttpServletRequest request);

    /**
     * 获取推荐用户(先从缓存查, 若缓存无,就去查数据库)
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    BaseResponse<Page<User>> userRecommend(long pageSize, long pageNum, HttpServletRequest request);

    /**
     * 从数据库获取目标用户的推荐用户
     * @param pageNum
     * @param pageSize
     * @param userId 目标用户的id
     * @return
     */
    Page<User> userRecommendByDatabase(long pageNum,long pageSize,  long userId);

    /**
     * 获取当前登录用户
     * @param request
     * @return 返回当前登录用户,  如果没登录就报错
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 校验是否是管理员
     * @param request
     * @return true --是 ，false --不是
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 校验是否已登录
     * @param request
     * @return 不报错 --已登录
     */
    void checkIsLogin(HttpServletRequest request);
}
