package com.jepai.exam.modules.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试安排实体
 */
@Data
@TableName("exam_arrange")
public class ExamArrange {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试名称 */
    private String name;

    /** 关联试卷ID */
    private Long paperId;

    /** 考试类型：1-正式考试 2-练习模式 3-补考 */
    private Integer examType;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 考试时长（分钟，若为0则以试卷设置为准） */
    private Integer duration;

    /** 参考人员范围（JSON：{type:1/2,ids:[...]}，1-全体 2-指定） */
    private String attendeeScope;

    /** 是否允许查看成绩：0-否 1-是 */
    private Integer showScore;

    /** 是否允许查看答案解析：0-否 1-是 */
    private Integer showAnalysis;

    /** 最大答题次数（0=不限制） */
    private Integer maxAttempts;

    /** 防切屏次数限制（0=不限制） */
    private Integer switchScreenLimit;

    /** 是否开启人脸核验：0-否 1-是 */
    private Integer faceVerify;

    /** 考试状态：0-草稿 1-已发布 2-进行中 3-已结束 4-评卷中 5-已完成 */
    private Integer status;

    /** 所属机构ID */
    private Long orgId;

    /** 创建人ID */
    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
