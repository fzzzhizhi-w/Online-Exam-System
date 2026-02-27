package com.jepai.exam.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jepai.exam.common.exception.BusinessException;
import com.jepai.exam.modules.user.dto.UserQueryDTO;
import com.jepai.exam.modules.user.dto.UserSaveDTO;
import com.jepai.exam.modules.user.entity.SysUser;
import com.jepai.exam.modules.user.mapper.SysUserMapper;
import com.jepai.exam.modules.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = getOne(new QueryWrapper<SysUser>().select("*")
                .eq("username", username)
                .eq("deleted", 0));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user;
    }

    @Override
    public Page<SysUser> pageUsers(UserQueryDTO query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(SysUser::getUsername, query.getKeyword())
                        .or().like(SysUser::getRealName, query.getKeyword()))
                .eq(query.getOrgId() != null, SysUser::getOrgId, query.getOrgId())
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
                .eq(StringUtils.hasText(query.getRoleCode()), SysUser::getRoleCode, query.getRoleCode())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByDesc(SysUser::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(UserSaveDTO dto) {
        if (dto.getId() == null) {
            long count = lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).count();
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
            SysUser user = new SysUser();
            copyDtoToEntity(dto, user);
            String rawPassword = StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "exam@123456";
            user.setPassword(passwordEncoder.encode(rawPassword));
            save(user);
        } else {
            SysUser user = getById(dto.getId());
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            copyDtoToEntity(dto, user);
            if (StringUtils.hasText(dto.getPassword())) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            updateById(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        lambdaUpdate().eq(SysUser::getId, id).set(SysUser::getPassword, passwordEncoder.encode(newPassword)).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        lambdaUpdate().eq(SysUser::getId, id).set(SysUser::getStatus, status).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserSaveDTO dto) {
        lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getRealName, dto.getRealName())
                .set(SysUser::getPhone, dto.getPhone())
                .set(SysUser::getEmail, dto.getEmail())
                .set(SysUser::getGender, dto.getGender())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        lambdaUpdate().eq(SysUser::getId, userId)
                .set(SysUser::getPassword, passwordEncoder.encode(newPassword)).update();
    }

    private void copyDtoToEntity(UserSaveDTO dto, SysUser user) {
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setOrgId(dto.getOrgId());
        user.setDeptId(dto.getDeptId());
        user.setJobNumber(dto.getJobNumber());
        user.setRoleCode(dto.getRoleCode());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user.setRemark(dto.getRemark());
    }
}
