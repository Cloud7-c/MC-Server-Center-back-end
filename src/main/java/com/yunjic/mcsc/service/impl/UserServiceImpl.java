package com.yunjic.mcsc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjic.mcsc.common.ResponseCode;
import com.yunjic.mcsc.exception.BusinessException;
import com.yunjic.mcsc.mapper.UserMapper;
import com.yunjic.mcsc.pojo.model.User;
import com.yunjic.mcsc.service.UserService;
import com.yunjic.mcsc.util.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.yunjic.mcsc.constant.UserConstant.ADMIN_ROLE;
import static com.yunjic.mcsc.constant.UserConstant.CURRENT_USER_SESSION_KEY;

/**
 * userService实现类
 *
 * @author yunji
 * @createTime 2023-03-28 23:02:12
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    UserMapper userMapper;

    @Value("${allow-register}")
    boolean allowRegister;

    @Override
    public User login(String userName, String password, HttpServletRequest request) {
        log.info("接收到用户登录请求：{userName: " + userName + ", password: " + password + "}");
        //用户名和密码不能为空
        if(StringUtils.isAnyBlank(userName,password)){
            throw new BusinessException(ResponseCode.NULL_ERROR);
        }

        //用户名应小于64
        if(userName.length() < 4 || userName.length() >= 64){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }

        //密码应>=8位且<=64位
        if(password.length() < 8 || password.length() > 64){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }

        //校验用户名的组成是否合法
        if(!userNamePattern.matcher(userName).find()){
            throw new BusinessException(ResponseCode.LOGIN_FAIL);
        }

        //按用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        User originalUser = userMapper.selectOne(queryWrapper);
        if(originalUser == null){
            throw new BusinessException(ResponseCode.LOGIN_FAIL);
        }

        //加盐加密后与数据库中的密码比较
        String encryptedPassword = EncryptUtil.Encrypt(password, originalUser.getSalt());
        if(!encryptedPassword.equals(originalUser.getUserPassword())){
            throw new BusinessException(ResponseCode.LOGIN_FAIL);
        }

        //返回脱敏后的用户
        User safetyUser = getSafetyUser(originalUser);
        request.getSession().setAttribute(CURRENT_USER_SESSION_KEY,safetyUser);
        return safetyUser;
    }

    @Override
    public long register(String userName, String password, String checkPassword, String email) {
        log.info("接收到用户注册请求：{userName: " + userName + ", email: " + email + ", password: " + password + "}");
        //输入不能为空
        if(StringUtils.isAnyBlank(userName, password, checkPassword, email)){
            throw new BusinessException(ResponseCode.NULL_ERROR);
        }

        //密码应>=8位<=64位
        if(password.length() < 8 || password.length() > 64){
            throw new BusinessException(ResponseCode.ILLEGAL_PASSWORD);
        }

        //密码和确认密码应一致
        if(!password.equals(checkPassword)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }

        //检查用户名是否合法
        if(userName.length() < 4 || userName.length() >= 64 || !userNamePattern.matcher(userName).find()){
            throw new BusinessException(ResponseCode.ILLEGAL_USER_NAME);
        }
        //检查邮箱是否合法
        if(!emailPattern.matcher(email).find()){
            throw new BusinessException(ResponseCode.ILLEGAL_EMAIL);
        }

        //查询是否已有该用户名的用户
        User user = userMapper.findUserByUserName(userName);
        if(user != null){
            throw new BusinessException(ResponseCode.USER_NAME_IS_OCCUPIED);
        }

        //拒绝注册写在这里而不是前面是为了方便前端演示功能
        if(!allowRegister){
            throw new BusinessException(ResponseCode.NOT_ALLOW_REGISTER);
        }

        //密码加盐加密
        String salt = EncryptUtil.getSalt();
        String encryptedPassword = EncryptUtil.Encrypt(password,salt);

        //存入数据库中
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setUserPassword(encryptedPassword);
        newUser.setEmail(email);
        newUser.setSalt(salt);

        boolean saveResult = this.save(newUser);
        if(!saveResult){
            throw new BusinessException(ResponseCode.SYSTEM_ERROR);
        }

        return newUser.getId();
    }

    @Override
    public User getSafetyUser(User originalUser) {
        if(originalUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originalUser.getId());
        safetyUser.setUserName(originalUser.getUserName());
        safetyUser.setUserPassword("");
        safetyUser.setEmail(originalUser.getEmail());
        safetyUser.setRole(originalUser.getRole());
        safetyUser.setCreateTime(originalUser.getCreateTime());
        safetyUser.setUpdateTime(originalUser.getUpdateTime());
        safetyUser.setDeleted(0);
        safetyUser.setSalt("");

        return safetyUser;
    }

    @Override
    public boolean activateUser(String code) {
        //todo 用户激活
        return false;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(CURRENT_USER_SESSION_KEY);
        return true;
    }

    @Override
    public List<User> searchUser(String id, String userName, String email, String role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isBlank(id)){
            if(StringUtils.isNumeric(id)){
                queryWrapper.eq("id", Long.parseLong(id));
            }
            else{
                queryWrapper.eq("id", -1);
            }
        }
        if(!StringUtils.isBlank(userName)){
            queryWrapper.like("user_name", userName);
        }
        if(!StringUtils.isBlank(email)){
            queryWrapper.like("email", email);
        }
        if(!StringUtils.isBlank(role) && StringUtils.isNumeric(role)){
            queryWrapper.eq("role", Long.parseLong(role));
        }
        List<User> userList = this.list(queryWrapper);
        for(User user: userList){
            if(user.getRole() == ADMIN_ROLE){
                user.setUserName("管理员(fake name)");
            }
        }
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public boolean updateUser(User user) {
        Long id = user.getId();
        String userName = user.getUserName();
        String email = user.getEmail();
        Integer role = user.getRole();
        //校验用户信息是否合法
        if(StringUtils.isAnyBlank(userName, email)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        if(id <= 0){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        //检查用户名是否合法
        if(userName.length() < 4 || userName.length() >= 64 || !userNamePattern.matcher(userName).find()){
            throw new BusinessException(ResponseCode.ILLEGAL_USER_NAME);
        }
        //检查邮箱是否合法
        if(!emailPattern.matcher(email).find()){
            throw new BusinessException(ResponseCode.ILLEGAL_EMAIL);
        }
        //查询用户名是否已被占用
        User queryUser = userMapper.findUserByUserName(userName);
        if(queryUser != null && queryUser.getId() != id){
            throw new BusinessException(ResponseCode.USER_NAME_IS_OCCUPIED);
        }

        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("id", id);
        userUpdateWrapper.set("user_name", userName);
        userUpdateWrapper.set("email", email);
        userUpdateWrapper.set("role", role);
        return this.update(userUpdateWrapper);
    }

    @Override
    public boolean deleteUser(long id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        return this.remove(queryWrapper);
    }

}