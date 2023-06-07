package com.yunjic.mcsc.exception;

import com.sun.corba.se.impl.io.TypeMismatchException;
import com.yunjic.mcsc.common.ResponseCode;
import com.yunjic.mcsc.common.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author yunji
 * @createTime 2023-03-31 15:29:54
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseData<String> businessException(BusinessException e){
        log.info("捕获BusinessException: " + e.getMessage());
        ResponseData<String> responseData = new ResponseData<>();
        responseData.setCode(e.getCode());
        responseData.setMessage(e.getMessage());
        responseData.setDescription(e.getDescription());
        return responseData;
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseData typeMismatchHandler(TypeMismatchException e){
        log.error("typeMismatchException:", e);
        return new ResponseData(ResponseCode.PARAMS_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseData runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException:", e);
        return new ResponseData(ResponseCode.SYSTEM_ERROR);
    }
    @ExceptionHandler(Exception.class)
    public ResponseData exceptionHandler(Exception e) {
        log.error("exception:", e);
        return new ResponseData(ResponseCode.SYSTEM_ERROR);
    }
}
