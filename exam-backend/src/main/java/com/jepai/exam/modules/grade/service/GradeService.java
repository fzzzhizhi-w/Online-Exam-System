package com.jepai.exam.modules.grade.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jepai.exam.modules.grade.entity.GradeDetail;

/**
 * 评卷服务接口
 */
public interface GradeService extends IService<GradeDetail> {

    /**
     * 异步自动评卷（客观题）
     * 由RabbitMQ消费者触发
     */
    void autoGrade(Long recordId);

    /**
     * 人工评分（主观题）
     */
    void manualGrade(Long detailId, Long graderId, Double score, String comment);

    /**
     * 仲裁评分
     */
    void arbitrateGrade(Long detailId, Long arbitratorId, Double score);

    /**
     * 获取待评卷列表（分配给评卷员的）
     */
    Page<GradeDetail> pageGradingTasks(Integer pageNum, Integer pageSize, Long graderId, Integer status);
}
