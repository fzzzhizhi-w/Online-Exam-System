package com.jepai.exam.modules.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jepai.exam.common.response.Result;
import com.jepai.exam.common.utils.SecurityUtils;
import com.jepai.exam.modules.exam.dto.AnswerSubmitDTO;
import com.jepai.exam.modules.exam.entity.ExamArrange;
import com.jepai.exam.modules.exam.entity.ExamRecord;
import com.jepai.exam.modules.exam.service.ExamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考试管理控制器
 */
@Api(tags = "考试管理")
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @ApiOperation("分页查询考试列表")
    @GetMapping("/page")
    public Result<Page<ExamArrange>> pageExams(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer status) {
        return Result.success(examService.pageExams(pageNum, pageSize, keyword, orgId, status));
    }

    @ApiOperation("获取考试详情")
    @GetMapping("/{id}")
    public Result<ExamArrange> getExam(@PathVariable Long id) {
        return Result.success(examService.getById(id));
    }

    @ApiOperation("新增/编辑考试")
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> saveExam(@RequestBody ExamArrange exam) {
        examService.saveExam(exam);
        return Result.success();
    }

    @ApiOperation("发布考试")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> publishExam(@PathVariable Long id) {
        examService.publishExam(id);
        return Result.success();
    }

    @ApiOperation("结束考试")
    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> endExam(@PathVariable Long id) {
        examService.endExam(id);
        return Result.success();
    }

    @ApiOperation("考生进入考试")
    @PostMapping("/{examId}/enter")
    public Result<Map<String, Object>> enterExam(@PathVariable Long examId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(examService.enterExam(examId, userId));
    }

    @ApiOperation("实时保存答题进度")
    @PostMapping("/answer/save")
    public Result<?> saveProgress(@RequestBody AnswerSubmitDTO dto) {
        examService.saveAnswerProgress(dto);
        return Result.success();
    }

    @ApiOperation("交卷")
    @PostMapping("/answer/submit")
    public Result<ExamRecord> submitExam(@RequestBody AnswerSubmitDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ExamRecord record = examService.submitExam(dto.getRecordId(), userId);
        return Result.success(record);
    }

    @ApiOperation("上报切屏事件")
    @PostMapping("/record/{recordId}/switch-screen")
    public Result<?> reportSwitchScreen(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        examService.reportSwitchScreen(recordId, userId);
        return Result.success();
    }

    @ApiOperation("强制收卷（监考员）")
    @PostMapping("/record/{recordId}/force-submit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> forceSubmit(@PathVariable Long recordId) {
        examService.forceSubmit(recordId);
        return Result.success();
    }

    @ApiOperation("查询答卷记录")
    @GetMapping("/records")
    public Result<Page<ExamRecord>> pageRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long userId) {
        return Result.success(examService.pageRecords(pageNum, pageSize, examId, userId));
    }
}
