package com.jepai.exam.modules.exam.dto;

import lombok.Data;

import java.util.Map;

/**
 * 答题提交 DTO
 */
@Data
public class AnswerSubmitDTO {
    /** 考试记录ID */
    private Long recordId;
    /** 答案Map：{questionId: answer} */
    private Map<Long, String> answers;
    /** 是否最终交卷 */
    private Boolean finalSubmit = false;
}
