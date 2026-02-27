package com.jepai.exam.modules.stats.service;

import java.util.Map;

/**
 * 统计分析服务接口
 */
public interface StatsService {

    Map<String, Object> getExamOverview(Long examId);

    Map<String, Object> getScoreDistribution(Long examId);

    Map<String, Object> getKnowledgeMastery(Long examId);

    Map<String, Object> getPersonalReport(Long recordId, Long userId);

    Map<String, Object> getMyHistory(Long userId, Integer pageNum, Integer pageSize);
}
