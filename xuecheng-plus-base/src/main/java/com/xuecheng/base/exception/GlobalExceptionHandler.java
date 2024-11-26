package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/28 10:30
 * @Description 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 方法处理的异常类型 -- 自定义异常
     * @param xueChengPlusException
     * @return RestErrorResponse
     */
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 响应状态码为500
    public RestErrorResponse customException(XueChengPlusException xueChengPlusException) {
        // 记录异常
        log.error("系统异常：{}", xueChengPlusException.getErrMessage());
        return new RestErrorResponse(xueChengPlusException.getErrMessage());
    }

    /**
     * 捕获自定义异常外的其他异常
     * @param exception
     * @return RestErrorResponse
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 响应状态码为500
    public RestErrorResponse extraException(Exception exception) {
        // 记录异常
        log.error("系统异常：{}", exception.getMessage(), exception);
        // 处理security中的AccessDeniedException异常，同时避免引入security依赖
        if (exception.getMessage().equals("不允许访问")) {
            return new RestErrorResponse("无权限");
        }
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }

    /**
     * 处理 @Validate 异常信息
     * @param e 异常信息
     * @return 返回异常响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        // 校验的错误信息
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuffer errors = new StringBuffer();
        fieldErrors.forEach((error) -> errors.append(error.getDefaultMessage()).append(","));
        log.error("【参数校验异常】{}", errors, e);
        return new RestErrorResponse(errors.toString());
    }
}
