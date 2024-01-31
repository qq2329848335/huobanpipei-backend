package com.jiamian.huobanpipeibackend.constant;

public interface UserConstant {

    /**
     * 用户登录态键
     */
    static final String USER_LOGIN_STATE = "userLoginState";

    /**
     * 管理员角色
     */
    static final Integer MANAGER_ROLE = 1;

    /**
     * 默认角色
     */
    static final Integer DEFAULT_ROLE = 0;

    /**
     * 用户最多可加入的队伍数量
     */
    static final Integer MAX_JOIN_TEAM_NUMBER = 20;

    /**
     * 每个用户最多可创建的队伍数
     */
    static final Integer MAX_CRATE_TEAM_NUMBER =5;
}
