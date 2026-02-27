package com.jepai.exam.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jepai.exam.common.response.Result;
import com.jepai.exam.modules.system.entity.SysDict;
import com.jepai.exam.modules.system.mapper.SysDictMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 */
@Api(tags = "字典管理")
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictMapper dictMapper;

    @ApiOperation("根据类型查询字典")
    @GetMapping("/{dictType}")
    public Result<List<SysDict>> listByType(@PathVariable String dictType) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getSort);
        return Result.success(dictMapper.selectList(wrapper));
    }

    @ApiOperation("保存字典")
    @PostMapping("/save")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<?> saveDict(@RequestBody SysDict dict) {
        if (dict.getId() == null) {
            dictMapper.insert(dict);
        } else {
            dictMapper.updateById(dict);
        }
        return Result.success();
    }

    @ApiOperation("删除字典")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<?> deleteDict(@PathVariable Long id) {
        dictMapper.deleteById(id);
        return Result.success();
    }
}
