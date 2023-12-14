package com.jiamian.huobanpipeibackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
    List<User> searchUserByTags(List<String> tagList);


    /**
     * 根据标签搜索用户(只要包含一个标签就行)
     * @param tagList 要求用户包含的标签列表
     * @return 脱敏后用户集合
     */
    List<User> searchUserByTagsOr(List<String> tagList);
}
