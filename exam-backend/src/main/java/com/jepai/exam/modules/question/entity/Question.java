package com.jepai.exam.modules.question.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目实体
 */
@Data
@TableName("exam_question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目标题/内容（支持富文本） */
    private String content;

    /** 题型：1-单选 2-多选 3-判断 4-填空 5-简答 6-综合案例 */
    private Integer type;

    /** 选项（JSON格式存储，客观题使用） */
    private String options;

    /** 正确答案（客观题：选项序号；主观题：参考答案） */
    private String answer;

    /** 答案解析 */
    private String analysis;

    /** 所属科目ID */
    private Long subjectId;

    /** 知识点标签（JSON数组） */
    private String knowledgeTags;

    /** 难度：1-容易 2-较易 3-中等 4-较难 5-困难 */
    private Integer difficulty;

    /** 区分度（0.00~1.00） */
    private Double discrimination;

    /** 分值（默认分值） */
    private Double score;

    /** 审核状态：0-待审核 1-已审核 2-已拒绝 */
    private Integer auditStatus;

    /** 审核人ID */
    private Long auditorId;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核备注 */
    private String auditRemark;

    /** 使用次数 */
    private Integer usedCount;

    /** 状态：0-草稿 1-正常 2-作废 */
    private Integer status;

    /** 所属机构ID（数据权限隔离） */
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
