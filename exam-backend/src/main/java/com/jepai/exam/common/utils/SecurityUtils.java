package com.jepai.exam.common.utils;

import com.jepai.exam.modules.user.entity.SysUser;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户
     */
    public static SysUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof SysUser) {
            return (SysUser) principal;
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        SysUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        SysUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
}
