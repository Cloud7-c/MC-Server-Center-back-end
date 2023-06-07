package com.yunjic.mcsc.constant;

/**
 * 用户常量
 *
 * @author yunji
 * @createTime 2023-03-29 14:11:18
 */
public interface UserConstant {

    /**
     * 当前登录用户session键
     */
    String CURRENT_USER_SESSION_KEY = "current_login_user";

    /**
     * websocket保存用户名的session键
     */
    String USER_NAME_KEY = "user_name";

    /**
     * 管理员权限用户
     */
    int ADMIN_ROLE = 0;

    /**
     * 普通用户
     */
    int NORMAL_ROLE = 1;

    /**
     * 被封禁用户
     */
    int BANNED_ROLE = -2;

    /**
     * 未激活用户
     */
    int INACTIVE_ROLE = -1;

    /**
     * 游客，作项目展示用
     */
    int VISITOR_ROLE = 2;
}
