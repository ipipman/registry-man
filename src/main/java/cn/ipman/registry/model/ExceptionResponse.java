package cn.ipman.registry.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 全局异常返回
 *
 * @Author IpMan
 * @Date 2024/4/14 19:59
 */
@Data
@AllArgsConstructor
public class ExceptionResponse {
    private String errCode;
    private String errMessage;
}
