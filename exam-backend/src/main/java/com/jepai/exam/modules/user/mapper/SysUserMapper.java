package com.jepai.exam.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jepai.exam.modules.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询用户（含机构、部门信息）
     */
    Page<SysUser> selectUserPage(Page<SysUser> page,
                                  @Param("keyword") String keyword,
                                  @Param("orgId") Long orgId,
                                  @Param("deptId") Long deptId,
                                  @Param("roleCode") String roleCode,
                                  @Param("status") Integer status);
}
