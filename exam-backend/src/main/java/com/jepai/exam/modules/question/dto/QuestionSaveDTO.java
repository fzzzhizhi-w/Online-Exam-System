package com.jepai.exam.modules.question.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 题目保存 DTO
 */
@Data
public class QuestionSaveDTO {
    private Long id;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    @NotNull(message = "题型不能为空")
    private Integer type;

    /** 选项（JSON格式：[{label:"A",content:"..."},...] ） */
    private String options;

    @NotBlank(message = "正确答案不能为空")
    private String answer;

    private String analysis;

    @NotNull(message = "所属科目不能为空")
    private Long subjectId;

    private String knowledgeTags;
    private Integer difficulty = 3;
    private Double score = 1.0;
    private Long orgId;
}
