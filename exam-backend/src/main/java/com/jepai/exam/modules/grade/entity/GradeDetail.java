package com.jepai.exam.modules.grade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评卷明细实体（主观题评分记录）
 */
@Data
@TableName("exam_grade_detail")
public class GradeDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 答卷记录ID */
    private Long recordId;

    /** 题目ID */
    private Long questionId;

    /** 第一评分员ID */
    private Long grader1Id;

    /** 第一评分员得分 */
    private Double score1;

    /** 第一评分员评语 */
    private String comment1;

    /** 第一评分时间 */
    private LocalDateTime gradeTime1;

    /** 第二评分员ID（双评机制） */
    private Long grader2Id;

    /** 第二评分员得分 */
    private Double score2;

    /** 第二评分员评语 */
    private String comment2;

    /** 第二评分时间 */
    private LocalDateTime gradeTime2;

    /** 仲裁得分（分差超阈值时启用） */
    private Double arbitrateScore;

    /** 仲裁人ID */
    private Long arbitratorId;

    /** 仲裁时间 */
    private LocalDateTime arbitrateTime;

    /** 最终得分 */
    private Double finalScore;

    /** 智能预评分（基于关键词匹配） */
    private Double aiPreScore;

    /** AI预评依据（JSON） */
    private String aiScoreBasis;

    /** 状态：0-待评 1-已评（单评/双评一致）2-待仲裁 3-已仲裁 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
