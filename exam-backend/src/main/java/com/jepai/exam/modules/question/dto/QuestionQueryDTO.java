package com.jepai.exam.modules.question.dto;

import lombok.Data;

/**
 * 题目查询 DTO
 */
@Data
public class QuestionQueryDTO {
    private String keyword;
    private Integer type;
    private Long subjectId;
    private Integer difficulty;
    private Integer auditStatus;
    private Integer status;
    private Long orgId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
