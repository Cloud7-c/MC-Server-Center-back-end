package com.yunjic.mcsc.pojo.model;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户权限等级(0->最高权限，1->普通用户，-2->被封禁，-1->未验证，2/3/4/..->其他权限组)
     */
    private Integer role;

    /**
     * 创建时间 
     */
    private Date createTime;

    /**
     *  更新时间
     */
    private Date updateTime;

    /**
     * 是否被逻辑删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 盐值
     */
    private String salt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}