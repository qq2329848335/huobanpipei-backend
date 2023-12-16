package com.jiamian.huobanpipeibackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.constant.UserConstant;
import com.jiamian.huobanpipeibackend.exception.BusinessException;
import com.jiamian.huobanpipeibackend.mapper.UserMapper;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author 加棉
 * @since 2023-12-01
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    UserMapper userMapper;

    /**
     * 盐值,混淆密码
     */
    final String SALT = "jiamian";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            //为空
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号、密码、校验密码都不能为空");
        }
        if (userAccount.length() < 4) {
            //账号长度小于4位
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于4位");
        }
        if (userPassword.length() < 8) {
            //密码长度小于8位
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        }


        String validPattern = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：\"”“’。，、？]|\n|\r|\t";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            //包含特殊字符
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        if (!userPassword.equals(checkPassword)) {
            //两次密码不一致
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }


        //把需要通过数据库查询的放在后面,可减少性能的开销
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<User> lambda = userQueryWrapper.lambda();
        LambdaQueryWrapper<User> eq = lambda.eq(User::getUserAccount, userAccount);
        if (userMapper.selectCount(eq) > 0) {
            //账号已经存在
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已经存在");
        }


        //2.加密 --使用spring5提供的工具库
        //加个前缀,更加混淆密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        int insert = userMapper.insert(user);
        if (insert <= 0) {
            //数据库出现问题,插入失败
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册时，数据库插入数据失败");
        }
        return user.getId();
    }

    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            //为空
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号、密码、校验密码都不能为空");
        }
        if (userAccount.length() < 4) {
            //账号长度小于4位
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于4位");
        }
        if (userPassword.length() < 8) {
            //密码长度小于8位
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        }


        String validPattern = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：\"”“’。，、？]|\n|\r|\t";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            //包含特殊字符
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        //2.校验密码是否正确,要和数据库中的密文密码去对比
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<User> lambda = userQueryWrapper.lambda();
        LambdaQueryWrapper<User> eq = lambda.eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User user = userMapper.selectOne(eq);
        if (user == null) {
            //登录失败,账号不存在 或密码错误
            log.info("Login failed, userAccount does not exist or userPassword is incorrect!");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录失败,账号不存在 或密码错误");
        }


        //3.对用户信息脱敏
        // 这里新new一个对象返回,是为了方便看返回了那些信息
        User safetyUser = this.getSafetyUser(user);


        //4.记录用户的登录态(session)
        HttpSession session = request.getSession();
        session.setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);


        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> userSearch(String username) {
        //查询
        LambdaQueryWrapper<User> lambdaQueryWrapper = new QueryWrapper<User>().lambda();
        if (StringUtils.isNotBlank(username)) {
            lambdaQueryWrapper.like(User::getUsername, username);
        }
        List<User> userList = userMapper.selectList(lambdaQueryWrapper);
        //脱敏
        return userList.stream().map(user1 -> {
            user1.setUserPassword(null);
            return user1;
        }).collect(Collectors.toList());
    }

    @Override
    public User getSafetyUser(User user) {
        if (user == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setUpdateTime(user.getUpdateTime());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setTags(user.getTags());
        safetyUser.setProfile(user.getProfile());
        return safetyUser;
    }

    /**
     * 根据标签搜索用户(包含所有搜索标签)
     * @param tagList 要求用户包含的标签列表
     * @return 脱敏后用户集合
     */
    @Override
    public List<User> searchUserByTagAnd(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索标签不能为空");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new QueryWrapper<User>().lambda();
        //拼接and查询 like '%Java' and like '%Python%'
        for (String tag : tagList) {
            if (StringUtils.isNotEmpty(tag)) {
                userLambdaQueryWrapper.like(User::getTags, tag);
            }
        }
        List<User> userList = userMapper.selectList(userLambdaQueryWrapper);
        //脱敏
        return userList.stream().map(user -> {
            user.setUserPassword(null);
            return user;
        }).collect(Collectors.toList());


    }

    /**
     * 根据标签搜索用户(只要包含一个标签就行)
     * @param tagList 要求用户包含的标签列表
     * @return 脱敏后用户集合
     */
    @Override
    public List<User> searchUserByTagsOr(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索标签不能为空");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new QueryWrapper<User>().lambda();
        //拼接and查询 like '%Java' or like '%Python%'
        for (String tag : tagList) {
            if (StringUtils.isNotEmpty(tag)) {
                userLambdaQueryWrapper.like(User::getTags, tag);
                userLambdaQueryWrapper.or();
            }
        }
        List<User> userList = userMapper.selectList(userLambdaQueryWrapper);
        //脱敏
        return userList.stream().map(user -> {
            user.setUserPassword(null);
            return user;
        }).collect(Collectors.toList());
    }
}
