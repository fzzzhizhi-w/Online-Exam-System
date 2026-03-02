package com.jepai.exam.modules.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jepai.exam.common.response.Result;
import com.jepai.exam.modules.user.dto.UserQueryDTO;
import com.jepai.exam.modules.user.dto.UserSaveDTO;
import com.jepai.exam.modules.user.entity.SysUser;
import com.jepai.exam.modules.user.service.SysUserService;
import com.jepai.exam.common.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @ApiOperation("分页查询用户")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<Page<SysUser>> pageUsers(UserQueryDTO query) {
        return Result.success(userService.pageUsers(query));
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<SysUser> getUser(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @ApiOperation("创建/更新用户")
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<?> saveUser(@RequestBody @Valid UserSaveDTO dto) {
        userService.saveUser(dto);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @ApiOperation("重置密码")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.getOrDefault("password", "exam@123456"));
        return Result.success();
    }

    @ApiOperation("修改用户状态")
    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/me")
    public Result<SysUser> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getById(userId));
    }

    @ApiOperation("修改个人信息")
    @PutMapping("/me/profile")
    public Result<?> updateProfile(@RequestBody UserSaveDTO dto) {
        userService.updateProfile(SecurityUtils.getCurrentUserId(), dto);
        return Result.success();
    }

    @ApiOperation("修改密码")
    @PutMapping("/me/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body) {
        userService.changePassword(
                SecurityUtils.getCurrentUserId(),
                body.get("oldPassword"),
                body.get("newPassword")
        );
        return Result.success();
    }

    @ApiOperation("批量删除用户")
    @DeleteMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<?> batchDelete(@RequestBody List<Long> ids) {
        userService.batchDelete(ids);
        return Result.success();
    }

    @ApiOperation("批量禁用用户")
    @PutMapping("/batch-disable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<?> batchDisable(@RequestBody List<Long> ids) {
        userService.batchUpdateStatus(ids, 0);
        return Result.success();
    }

    @ApiOperation("批量启用用户")
    @PutMapping("/batch-enable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<?> batchEnable(@RequestBody List<Long> ids) {
        userService.batchUpdateStatus(ids, 1);
        return Result.success();
    }

    @ApiOperation("检查用户名是否可用")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username,
                                         @RequestParam(required = false) Long excludeId) {
        return Result.success(userService.isUsernameAvailable(username, excludeId));
    }

    @ApiOperation("检查手机号是否可用")
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(@RequestParam String phone,
                                      @RequestParam(required = false) Long excludeId) {
        return Result.success(userService.isPhoneAvailable(phone, excludeId));
    }
}
