package com.jepai.exam.modules.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试记录实体（考生答题记录）
 */
@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联考试ID */
    private Long examId;

    /** 考生ID */
    private Long userId;

    /** 答卷内容（JSON格式：{questionId: answer, ...}） */
    private String answers;

    /** 总分 */
    private Double totalScore;

    /** 客观题得分 */
    private Double objectiveScore;

    /** 主观题得分 */
    private Double subjectiveScore;

    /** 是否通过：0-未通过 1-通过 */
    private Integer passed;

    /** 状态：0-进行中 1-已交卷 2-评卷中 3-已完成 */
    private Integer status;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 交卷时间 */
    private LocalDateTime submitTime;

    /** 用时（秒） */
    private Integer usedTime;

    /** 切屏次数 */
    private Integer switchCount;

    /** IP地址 */
    private String ipAddress;

    /** 是否雷同卷：0-否 1-是 */
    private Integer isSimilar;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
