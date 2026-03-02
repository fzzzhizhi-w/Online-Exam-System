package com.jepai.exam.modules.paper.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jepai.exam.common.response.Result;
import com.jepai.exam.modules.paper.entity.Paper;
import com.jepai.exam.modules.paper.service.PaperService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 试卷管理控制器
 */
@Api(tags = "试卷管理")
@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    @ApiOperation("分页查询试卷")
    @GetMapping("/page")
    public Result<Page<Paper>> pagePapers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgId) {
        return Result.success(paperService.pagePapers(pageNum, pageSize, keyword, orgId));
    }

    @ApiOperation("获取试卷详情")
    @GetMapping("/{id}")
    public Result<Paper> getPaper(@PathVariable Long id) {
        return Result.success(paperService.getById(id));
    }

    @ApiOperation("新增/编辑试卷")
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> savePaper(@RequestBody Paper paper) {
        paperService.savePaper(paper);
        return Result.success();
    }

    @ApiOperation("删除试卷")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> deletePaper(@PathVariable Long id) {
        paperService.deletePaper(id);
        return Result.success();
    }

    @ApiOperation("发布试卷")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> publishPaper(@PathVariable Long id) {
        paperService.publishPaper(id);
        return Result.success();
    }

    @ApiOperation("复制试卷")
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<Paper> copyPaper(@PathVariable Long id) {
        return Result.success(paperService.copyPaper(id));
    }
}
