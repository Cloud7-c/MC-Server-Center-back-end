package com.yunjic.mcsc.common;

public enum ResponseCode {
    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(10000, "请求参数错误", ""),
    NULL_ERROR(10001, "请求数据为空", ""),
    USER_NAME_IS_OCCUPIED(10002, "用户名已被占用", ""),
    ILLEGAL_USER_NAME(10003, "用户名不得少于4位，且必须以字母开头，由字母、下划线、数字组成", ""),
    ILLEGAL_EMAIL(10004, "无效的邮箱", ""),
    ILLEGAL_PASSWORD(10005, "密码长度需不少于8位且少于64位", ""),
    LOGIN_FAIL(10006, "用户名或密码错误", ""),
    NOT_LOGIN(10100, "未登录", ""),
    NO_AUTH(10101, "无权限", ""),
    SYSTEM_ERROR(50000, "系统异常", ""),
    MC_SERVER_CONNECTION_ERROR(50001, "Minecraft服务器连接错误", ""),
    NOT_ALLOW_REGISTER(10200, "注册接口未开放", "");

    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    ResponseCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

}
