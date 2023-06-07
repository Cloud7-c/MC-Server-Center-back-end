package com.yunjic.mcsc.pojo.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 封装用户登录请求
 *
 * @author yunji
 * @createTime 2023-03-31 17:10:24
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 6581825900048587657L;

    private String userName;
    private String password;
}
