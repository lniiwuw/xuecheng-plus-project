package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/28 10:29
 * @Description 和前端约定响应的异常信息类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestErrorResponse {
    private String errorMessage;
}
