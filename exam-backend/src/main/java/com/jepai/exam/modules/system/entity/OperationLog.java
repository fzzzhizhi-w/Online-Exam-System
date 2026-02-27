package com.jepai.exam.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作模块 */
    private String module;

    /** 操作描述 */
    private String operation;

    /** 请求方法 */
    private String method;

    /** 请求URL */
    private String requestUrl;

    /** 请求方式 */
    private String requestMethod;

    /** 请求IP */
    private String requestIp;

    /** 请求参数 */
    private String requestParams;

    /** 响应结果 */
    private String responseResult;

    /** 操作状态：0-失败 1-成功 */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
