package com.jepai.exam.modules.auth.dto;

import com.jepai.exam.modules.user.entity.SysUser;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
@Builder
public class LoginVO {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private SysUser userInfo;
}
