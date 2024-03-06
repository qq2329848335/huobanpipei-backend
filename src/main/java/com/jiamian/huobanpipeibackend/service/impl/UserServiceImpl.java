package com.jiamian.huobanpipeibackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jiamian.huobanpipeibackend.common.BaseResponse;
import com.jiamian.huobanpipeibackend.common.ErrorCode;
import com.jiamian.huobanpipeibackend.common.ResultUtil;
import com.jiamian.huobanpipeibackend.constant.UserConstant;
import com.jiamian.huobanpipeibackend.exception.BusinessException;
import com.jiamian.huobanpipeibackend.mapper.UserMapper;
import com.jiamian.huobanpipeibackend.model.entity.User;
import com.jiamian.huobanpipeibackend.service.UserService;
import com.jiamian.huobanpipeibackend.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Resource
    RedisTemplate redisTemplate;

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

    @Override
    public int userUpdate(User user, HttpServletRequest request) {
        if (user==null||user.getId()==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = this.getLoginUser(request);
        if (loginUser.getId()!=user.getId()&&!isAdmin(request)){
            //不是当前用户修改自己的信息 并且不是管理员
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        int i = userMapper.updateById(user);
        if (i<=0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新数据失败");
        }
        return i;
    }


    @Override
    public BaseResponse<Page<User>> userRecommend(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        //从缓存中取
        String redisKey = String.format("yupao:user:recommend:%s",loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        if (userPage==null){
            //无缓存,查数据库
            userPage = this.userRecommendByDatabase(pageNum,pageSize,loginUser.getId());
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
                this.getSafetyUser(user)
        ).collect(Collectors.toList());
        userPage.setRecords(userList);

        return ResultUtil.success(userPage);
    }

    @Override
    public List<User> userMatch(long num, HttpServletRequest request) {
        if (num<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = this.getLoginUser(request);
        String loginUserTags = loginUser.getTags();
        //取出所有的用户
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //todo 为了减小用户匹配时间,这里只匹配推荐前10000条用户
        userLambdaQueryWrapper.le(User::getId,10000);
        userLambdaQueryWrapper.isNotNull(User::getTags);
        userLambdaQueryWrapper.select(User::getId,User::getTags);
        List<User> userList = this.list(userLambdaQueryWrapper);
        //下标 =》编辑距离(编辑距离越大,相似度就越小)
        TreeMap<Integer, Long> map = new TreeMap<>();
        for (int i=0;i<userList.size();i++) {
            User user = userList.get(i);

            //除掉tags为空的用户
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags)){
                continue;
            }

            //排除自己
            if (user.getId().equals(loginUser.getId())){
                continue;
            }

            //使用编辑距离算法,进行相似度计算
            Gson gson = new Gson();
            List<String> loginUserTagList = gson.fromJson(loginUserTags, new TypeToken<List<String>>(){}.getType());
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>(){}.getType());

            long distance = AlgorithmUtils.minDistance(loginUserTagList, userTagList);
            map.put(i,distance);
        }
        // 将TreeMap转换为List
        List<Map.Entry<Integer, Long>> sortList = new ArrayList<>(map.entrySet());

        // 根据value(即编辑距离)对List进行升序排序
        Collections.sort(sortList, new Comparator<Map.Entry<Integer, Long>>() {
            @Override
            public int compare(Map.Entry<Integer, Long> o1, Map.Entry<Integer, Long> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        //因为上面查询出的数据只有id和tags(数据不完整)
        // 所以,需要去数据库查完整的信息
        // 1.取出前n条 对应的id,去数据库查,
        //  因为数据库查询得到的结果是重新排序的,并没有按原先的排序
        // 2.所以要根据之前的排序,将完整的top N数据进行顺序
        ArrayList<User> topNUsers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Map.Entry<Integer, Long> entry = sortList.get(i);
            topNUsers.add(this.getSafetyUser(userList.get(entry.getKey())));
        }
        //记录top N 条数据对应的id(排序好的)
        ArrayList<Long> idSortList = new ArrayList<>();
        for (User user : topNUsers) {
            idSortList.add(user.getId());
        }
        //回数据库查询完整的信息
        userLambdaQueryWrapper.in(User::getId,idSortList);
        List<User> nuTopUserList = this.listByIds(idSortList);
        //排序
        ArrayList<User> finalUserList = new ArrayList<>();
        for (Long id : idSortList) {
            for (User user : nuTopUserList) {
                if (user.getId().equals(id)){
                    finalUserList.add(user);
                    break;
                }
            }
        }
        return finalUserList;
    }


    @Override
    public Page<User> userRecommendByDatabase(long pageNum, long pageSize, long userId) {
        //无缓存,查数据库
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        Page<User> userPage = this.page(new Page<User>(pageNum, pageSize), userQueryWrapper);
        return userPage;
    }


    @Override
    public User getLoginUser(HttpServletRequest request){
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Object loginUser = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User)loginUser;
    }


    @Override
    public boolean isAdmin(HttpServletRequest request){
        //校验权限
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user==null || UserConstant.MANAGER_ROLE.equals(user.getUserRole())){
            log.info("未登录或无管理员权限");
            return false;
        }
        return true;
    }


    @Override
    public void checkIsLogin(HttpServletRequest request){
        User loginUser = this.getLoginUser(request);
    }
}
