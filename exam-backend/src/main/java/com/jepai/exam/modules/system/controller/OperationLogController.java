package com.jepai.exam.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jepai.exam.common.response.Result;
import com.jepai.exam.modules.system.entity.OperationLog;
import com.jepai.exam.modules.system.mapper.OperationLogMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

/**
 * 操作日志控制器
 */
@Api(tags = "操作日志")
@RestController
@RequestMapping("/api/system/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogMapper logMapper;

    @ApiOperation("分页查询操作日志")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public Result<Page<OperationLog>> pageLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .like(StringUtils.hasText(username), OperationLog::getUsername, username)
                .like(StringUtils.hasText(module), OperationLog::getModule, module)
                .orderByDesc(OperationLog::getCreateTime);
        return Result.success(logMapper.selectPage(page, wrapper));
    }
}
