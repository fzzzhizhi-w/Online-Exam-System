package com.jepai.exam.modules.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jepai.exam.modules.exam.dto.AnswerSubmitDTO;
import com.jepai.exam.modules.exam.entity.ExamArrange;
import com.jepai.exam.modules.exam.entity.ExamRecord;

import java.util.Map;

/**
 * 考试服务接口
 */
public interface ExamService extends IService<ExamArrange> {

    Page<ExamArrange> pageExams(Integer pageNum, Integer pageSize, String keyword, Long orgId, Integer status);

    void saveExam(ExamArrange exam);

    void publishExam(Long examId);

    void endExam(Long examId);

    /**
     * 考生进入考试（返回试卷内容+个人答题进度）
     */
    Map<String, Object> enterExam(Long examId, Long userId);

    /**
     * 实时保存答题进度（存入Redis）
     */
    void saveAnswerProgress(AnswerSubmitDTO dto);

    /**
     * 考生交卷
     */
    ExamRecord submitExam(Long recordId, Long userId);

    /**
     * 监考员强制收卷
     */
    void forceSubmit(Long recordId);

    /**
     * 上报切屏事件
     */
    void reportSwitchScreen(Long recordId, Long userId);

    /**
     * 查看考生考试记录列表
     */
    Page<ExamRecord> pageRecords(Integer pageNum, Integer pageSize, Long examId, Long userId);
}
