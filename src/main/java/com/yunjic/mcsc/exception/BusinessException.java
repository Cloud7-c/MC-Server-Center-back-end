package com.yunjic.mcsc.exception;

import com.yunjic.mcsc.common.ResponseCode;

/**
 * 自定义异常
 * 
 * @author yunji
 * @createTime 2023-03-31 15:28:24
 */
public class BusinessException extends RuntimeException{
    private final int code;
    private final String description;

    /**
     * 直接写入参数构造BusinessException
     *
     * @param message 异常信息
     * @param code 异常代码
     * @param description 异常描述
     */
    public BusinessException(String message, int code, String description){
        super(message);
        this.code = code;
        this.description = description;
    }

    /**
     * 使用异常代码自动构造BusinessException
     *
     * @param responseCode 自定义枚举异常代码
     */
    public BusinessException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.description = responseCode.getDescription();
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
