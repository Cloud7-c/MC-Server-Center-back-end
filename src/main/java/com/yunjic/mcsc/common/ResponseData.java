package com.yunjic.mcsc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回信息
 *
 * @author yunji
 * @createTime 2023-03-30 20:55:30
 */
@Data
public class ResponseData<T> implements Serializable {
    private static final long serialVersionUID = -4495364454439217763L;
    private int code;
    private String message;
    private T data;
    private String description;

   public ResponseData(){}
    public ResponseData(ResponseCode responseCode){
        code = responseCode.getCode();
        message = responseCode.getMessage();
        description = responseCode.getDescription();
    }
    public static <T> ResponseData<T> success(T data){
        ResponseData<T> responseData = new ResponseData<>(ResponseCode.SUCCESS);
        responseData.setData(data);
        return responseData;
    }

}
