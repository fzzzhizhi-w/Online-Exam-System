package com.jepai.exam.modules.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
