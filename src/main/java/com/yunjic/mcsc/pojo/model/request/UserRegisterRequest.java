package com.yunjic.mcsc.pojo.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体封装
 *
 * @author yunji
 * @createTime 2023-03-31 17:13:44
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 4417809856227586054L;

    private String userName;
    private String password;
    private String checkPassword;
    private String email;
}
