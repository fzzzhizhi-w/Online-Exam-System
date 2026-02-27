package com.jepai.exam.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考试状态枚举
 */
@Getter
@AllArgsConstructor
public enum ExamStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    IN_PROGRESS(2, "进行中"),
    ENDED(3, "已结束"),
    GRADING(4, "评卷中"),
    COMPLETED(5, "已完成");

    private final Integer code;
    private final String name;
}
