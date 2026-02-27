package com.jepai.exam.modules.question.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 科目实体
 */
@Data
@TableName("exam_subject")
public class Subject {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 科目名称 */
    private String name;

    /** 科目编码 */
    private String code;

    /** 科目描述 */
    private String description;

    /** 所属机构ID */
    private Long orgId;

    /** 父科目ID（0表示顶级） */
    private Long parentId;

    /** 排序号 */
    private Integer sort;

    /** 状态：0-禁用 1-正常 */
    private Integer status;

    /** 创建人ID */
    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
