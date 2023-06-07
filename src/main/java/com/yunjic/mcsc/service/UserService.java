package com.yunjic.mcsc.service;

import com.yunjic.mcsc.pojo.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 针对表【user(用户表)】的数据库操作Service
 *
 * @author yunjic
 * @createTime 2023-03-28 23:02:12
 */
public interface UserService extends IService<User> {

    /**
     * 用于正则匹配用户名是否合法，匹配成功为合法用户名
     */
    public static final Pattern userNamePattern = Pattern.compile("^[a-zA-Z]+\\w*$");

    /**
     * 用于正则匹配邮箱是否合法，匹配成功为合法
     */
    public static final Pattern emailPattern = Pattern.compile("^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$");

    /**
     *
     * @param userName 用户名
     * @param password 用户密码
     * @return 脱敏后的用户信息
     */
    User login(String userName, String password, HttpServletRequest request);

    /**
     *
     * @param userName 用户名
     * @param password 用户密码
     * @param checkPassword 确认密码
     * @param email 邮箱
     * @return 注册的用户id
     */
    long register(String userName, String password, String checkPassword, String email);

    /**
     *
     * @param originalUser 原始未脱敏用户
     * @return 脱敏后的用户
     */
    User getSafetyUser(User originalUser);

    /**
     *
     * @param code 激活码
     * @return 是否激活成功
     */
    boolean activateUser(String code);

    /**
     *
     * @param request httpRequest
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     *
     * @param id 搜索的用户id
     * @param userName 模糊搜索用户名
     * @param email 模糊搜索邮箱
     * @param role 搜索的用户角色
     * @return 匹配搜索条件的用户（已脱敏），若搜索条件为空，返回所有用户
     */
    List<User> searchUser(String id, String userName, String email, String role);

    /**
     * 更新用户信息，仅支持更新用户名、用户邮箱、用户角色，更新日期将自动修改
     *
     * @param user 需要更新信息的用户，包含更新后的用户信息
     * @return 是否更新成功
     */
    boolean updateUser(User user);

    /**
     *
     * @param id 要删除的用户的id
     * @return 是否删除成功
     */
    boolean deleteUser(long id);

}