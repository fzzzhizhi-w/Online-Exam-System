package com.jepai.exam.modules.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户查询请求
 */
@Data
public class UserQueryDTO {
    private String keyword;
    private Long orgId;
    private Long deptId;
    private String roleCode;
    private Integer status;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
