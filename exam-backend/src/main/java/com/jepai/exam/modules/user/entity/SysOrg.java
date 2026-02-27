package com.jepai.exam.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机构实体
 */
@Data
@TableName("sys_org")
public class SysOrg {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 机构名称 */
    private String name;

    /** 机构编码 */
    private String code;

    /** 父机构ID（0表示顶级） */
    private Long parentId;

    /** 机构类型：1-学校 2-企业 3-培训机构 */
    private Integer type;

    /** 联系人 */
    private String contact;

    /** 联系电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 状态：0-禁用 1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
