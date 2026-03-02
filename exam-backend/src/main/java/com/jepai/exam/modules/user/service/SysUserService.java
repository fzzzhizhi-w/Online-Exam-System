package com.jepai.exam.modules.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jepai.exam.modules.user.dto.UserQueryDTO;
import com.jepai.exam.modules.user.dto.UserSaveDTO;
import com.jepai.exam.modules.user.entity.SysUser;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser>, UserDetailsService {

    /**
     * 分页查询用户
     */
    Page<SysUser> pageUsers(UserQueryDTO query);

    /**
     * 创建/更新用户
     */
    void saveUser(UserSaveDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 修改状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 修改个人信息
     */
    void updateProfile(Long userId, UserSaveDTO dto);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 批量删除用户
     */
    void batchDelete(java.util.List<Long> ids);

    /**
     * 批量修改状态
     */
    void batchUpdateStatus(java.util.List<Long> ids, Integer status);

    /**
     * 检查用户名是否可用
     */
    boolean isUsernameAvailable(String username, Long excludeId);

    /**
     * 检查手机号是否可用
     */
    boolean isPhoneAvailable(String phone, Long excludeId);
}
