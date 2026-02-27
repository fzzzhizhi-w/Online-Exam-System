package com.jepai.exam.modules.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户创建/编辑请求
 */
@Data
public class UserSaveDTO {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String phone;
    private String email;
    private Integer gender;

    @NotNull(message = "所属机构不能为空")
    private Long orgId;

    private Long deptId;
    private String jobNumber;

    @NotBlank(message = "角色不能为空")
    private String roleCode;

    private Integer status = 1;
    private String remark;
}
