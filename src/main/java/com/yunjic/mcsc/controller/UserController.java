package com.yunjic.mcsc.controller;

import com.yunjic.mcsc.common.ResponseCode;
import com.yunjic.mcsc.exception.BusinessException;
import com.yunjic.mcsc.pojo.model.User;
import com.yunjic.mcsc.pojo.model.request.UserLoginRequest;
import com.yunjic.mcsc.pojo.model.request.UserRegisterRequest;
import com.yunjic.mcsc.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.yunjic.mcsc.constant.UserConstant.CURRENT_USER_SESSION_KEY;
import static com.yunjic.mcsc.constant.UserConstant.ADMIN_ROLE;
import static com.yunjic.mcsc.constant.UserConstant.VISITOR_ROLE;

/**
 * 接收用户请求的接口
 *
 * @author yunji
 * @createTime 2023-03-30 20:50:04
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ResponseCode.NULL_ERROR);
        }
        String userName = userLoginRequest.getUserName();
        String password = userLoginRequest.getPassword();
        if(StringUtils.isAnyBlank(userName, password)){
            throw new BusinessException(ResponseCode.NULL_ERROR);
        }
        return userService.login(userName, password, request);
    }

    @PostMapping("/register")
    public long userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            throw new BusinessException(ResponseCode.NULL_ERROR);
        }
        String userName = userRegisterRequest.getUserName();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String email = userRegisterRequest.getEmail();
        if(StringUtils.isAnyBlank(userName, password, checkPassword, email)){
            throw new BusinessException(ResponseCode.NULL_ERROR);
        }
        return userService.register(userName, password, checkPassword, email);
    }

    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ResponseCode.NULL_ERROR);
        }
        return (User) request.getSession().getAttribute(CURRENT_USER_SESSION_KEY);
    }

    @PostMapping("/logout")
    public boolean userLogout(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ResponseCode.NULL_ERROR);
        }
        if(!userService.userLogout(request)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        return true;
    }

    @GetMapping("/search")
    public List<User> searchUser(@RequestParam(value = "id", required = false)String id,
                                 @RequestParam(value = "userName", required = false)String userName,
                                 @RequestParam(value = "email", required = false)String email,
                                 @RequestParam(value = "role", required = false)String role,
                                 HttpServletRequest request)
    {
        if(!isAdmin(request) && !isVisitor(request)){
            throw new BusinessException(ResponseCode.NO_AUTH);
        }

        return userService.searchUser(id, userName, email, role);
    }

    @PostMapping("/update")
    public boolean updateUser(@RequestBody User user, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ResponseCode.NO_AUTH);
        }
        return userService.updateUser(user);
    }

    @GetMapping("/delete")
    public boolean deleteUser(@RequestParam String id, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ResponseCode.NO_AUTH);
        }

        if(!StringUtils.isNumeric(id)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        return userService.deleteUser(Long.parseLong(id));
    }

    private boolean isAdmin(HttpServletRequest request){
        User currentUser = (User)request.getSession().getAttribute(CURRENT_USER_SESSION_KEY);
        return currentUser != null && currentUser.getRole() == ADMIN_ROLE;
    }

    private boolean isVisitor(HttpServletRequest request){
        User currentUser = (User)request.getSession().getAttribute(CURRENT_USER_SESSION_KEY);
        return currentUser != null && currentUser.getRole() == VISITOR_ROLE;
    }
}
