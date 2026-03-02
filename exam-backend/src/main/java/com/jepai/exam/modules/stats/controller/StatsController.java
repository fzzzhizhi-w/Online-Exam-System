package com.jepai.exam.modules.stats.controller;

import com.jepai.exam.common.response.Result;
import com.jepai.exam.common.utils.SecurityUtils;
import com.jepai.exam.modules.stats.service.StatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计分析控制器
 */
@Api(tags = "统计分析")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @ApiOperation("考试整体统计（管理端）")
    @GetMapping("/exam/{examId}/overview")
    public Result<Map<String, Object>> examOverview(@PathVariable Long examId) {
        return Result.success(statsService.getExamOverview(examId));
    }

    @ApiOperation("分数段分布")
    @GetMapping("/exam/{examId}/score-distribution")
    public Result<Map<String, Object>> scoreDistribution(@PathVariable Long examId) {
        return Result.success(statsService.getScoreDistribution(examId));
    }

    @ApiOperation("知识点掌握度分析")
    @GetMapping("/exam/{examId}/knowledge-mastery")
    public Result<Map<String, Object>> knowledgeMastery(@PathVariable Long examId) {
        return Result.success(statsService.getKnowledgeMastery(examId));
    }

    @ApiOperation("个人成绩报告")
    @GetMapping("/my/report/{recordId}")
    public Result<Map<String, Object>> myReport(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(statsService.getPersonalReport(recordId, userId));
    }

    @ApiOperation("我的考试历史")
    @GetMapping("/my/history")
    public Result<Map<String, Object>> myHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(statsService.getMyHistory(userId, pageNum, pageSize));
    }

    @ApiOperation("管理端/教师端控制台统计")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(statsService.getDashboard());
    }

    @ApiOperation("学生端控制台统计")
    @GetMapping("/student/dashboard")
    public Result<Map<String, Object>> studentDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(statsService.getStudentDashboard(userId));
    }
}
