package com.jepai.exam.modules.paper.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 试卷实体
 */
@Data
@TableName("exam_paper")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 试卷名称 */
    private String name;

    /** 所属科目ID */
    private Long subjectId;

    /** 组卷模式：1-固定组卷 2-随机组卷 3-自适应组卷 */
    private Integer generateMode;

    /** 试卷内容（JSON格式：题目列表及分值配置） */
    private String content;

    /** 组卷规则（JSON格式：随机/自适应模式使用） */
    private String generateRule;

    /** 总分 */
    private Double totalScore;

    /** 及格分 */
    private Double passScore;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 是否乱序题目：0-否 1-是 */
    private Integer shuffleQuestion;

    /** 是否乱序选项：0-否 1-是 */
    private Integer shuffleOption;

    /** 状态：0-草稿 1-已发布 */
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
