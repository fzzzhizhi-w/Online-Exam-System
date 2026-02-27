package com.jepai.exam.modules.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jepai.exam.common.response.Result;
import com.jepai.exam.modules.question.dto.QuestionQueryDTO;
import com.jepai.exam.modules.question.dto.QuestionSaveDTO;
import com.jepai.exam.modules.question.entity.Question;
import com.jepai.exam.modules.question.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Map;

/**
 * 题库管理控制器
 */
@Api(tags = "题库管理")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @ApiOperation("分页查询题目")
    @GetMapping("/page")
    public Result<Page<Question>> pageQuestions(QuestionQueryDTO query) {
        return Result.success(questionService.pageQuestions(query));
    }

    @ApiOperation("获取题目详情")
    @GetMapping("/{id}")
    public Result<Question> getQuestion(@PathVariable Long id) {
        return Result.success(questionService.getById(id));
    }

    @ApiOperation("新增/编辑题目")
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> saveQuestion(@RequestBody @Valid QuestionSaveDTO dto) {
        questionService.saveQuestion(dto);
        return Result.success();
    }

    @ApiOperation("删除题目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }

    @ApiOperation("审核题目")
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<?> auditQuestion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        String remark = (String) body.getOrDefault("remark", "");
        questionService.auditQuestion(id, status, remark);
        return Result.success();
    }

    @ApiOperation("Excel批量导入题目")
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN', 'TEACHER')")
    public Result<Integer> importQuestions(
            @RequestParam MultipartFile file,
            @RequestParam Long orgId,
            @RequestParam Long subjectId) {
        int count = questionService.importFromExcel(file, orgId, subjectId);
        return Result.success("成功导入 " + count + " 道题目", count);
    }
}
