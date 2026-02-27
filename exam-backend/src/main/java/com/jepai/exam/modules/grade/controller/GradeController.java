package com.jepai.exam.modules.grade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jepai.exam.common.response.Result;
import com.jepai.exam.common.utils.SecurityUtils;
import com.jepai.exam.modules.grade.entity.GradeDetail;
import com.jepai.exam.modules.grade.service.GradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评卷控制器
 */
@Api(tags = "评卷管理")
@RestController
@RequestMapping("/api/grade")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @ApiOperation("获取评卷任务列表")
    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'GRADER', 'TEACHER')")
    public Result<Page<GradeDetail>> getTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long graderId,
            @RequestParam(required = false) Integer status) {
        return Result.success(gradeService.pageGradingTasks(pageNum, pageSize, graderId, status));
    }

    @ApiOperation("人工评分")
    @PostMapping("/tasks/{detailId}/grade")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'GRADER', 'TEACHER')")
    public Result<?> manualGrade(@PathVariable Long detailId, @RequestBody Map<String, Object> body) {
        Long graderId = SecurityUtils.getCurrentUserId();
        Double score = Double.valueOf(body.get("score").toString());
        String comment = (String) body.getOrDefault("comment", "");
        gradeService.manualGrade(detailId, graderId, score, comment);
        return Result.success();
    }

    @ApiOperation("仲裁评分")
    @PostMapping("/tasks/{detailId}/arbitrate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> arbitrateGrade(@PathVariable Long detailId, @RequestBody Map<String, Object> body) {
        Long arbitratorId = SecurityUtils.getCurrentUserId();
        Double score = Double.valueOf(body.get("score").toString());
        gradeService.arbitrateGrade(detailId, arbitratorId, score);
        return Result.success();
    }
}
