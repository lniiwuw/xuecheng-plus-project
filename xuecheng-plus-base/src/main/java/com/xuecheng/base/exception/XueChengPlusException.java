package com.xuecheng.base.exception;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/27 21:39
 * @Description 自定义项目异常类
 */
public class XueChengPlusException extends RuntimeException {
    private static final long serialVersionUID = 5565760508056698922L;
    private String errMessage;

    public XueChengPlusException() {
        super();
    }

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError) {
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new XueChengPlusException(errMessage);
    }
}
