package com.jepai.exam.modules.grade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jepai.exam.common.exception.BusinessException;
import com.jepai.exam.modules.exam.entity.ExamRecord;
import com.jepai.exam.modules.exam.mapper.ExamRecordMapper;
import com.jepai.exam.modules.grade.entity.GradeDetail;
import com.jepai.exam.modules.grade.mapper.GradeDetailMapper;
import com.jepai.exam.modules.grade.service.GradeService;
import com.jepai.exam.modules.question.entity.Question;
import com.jepai.exam.modules.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 评卷服务实现
 * 核心特色：双轨智能捷评引擎
 * - 客观题：秒级自动判分
 * - 主观题：基于关键词权重匹配的智能预评 + 双评机制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeServiceImpl extends ServiceImpl<GradeDetailMapper, GradeDetail> implements GradeService {

    private final ExamRecordMapper recordMapper;
    private final QuestionService questionService;
    private final ObjectMapper objectMapper;

    /** 双评分差阈值 */
    private static final double SCORE_DIFF_THRESHOLD = 3.0;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoGrade(Long recordId) {
        log.info("开始自动评卷，recordId: {}", recordId);
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            log.warn("评卷记录不存在: {}", recordId);
            return;
        }

        Map<Long, String> answers;
        try {
            answers = objectMapper.readValue(record.getAnswers(), new TypeReference<Map<Long, String>>() {});
        } catch (Exception e) {
            log.error("解析答案失败, recordId: {}", recordId, e);
            return;
        }

        double objectiveScore = 0.0;
        List<GradeDetail> subjectiveDetails = new ArrayList<>();

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            String userAnswer = entry.getValue();
            Question question = questionService.getById(questionId);
            if (question == null) continue;

            if (isObjectiveQuestion(question.getType())) {
                // 客观题自动判分
                double score = judgeObjectiveAnswer(question, userAnswer);
                objectiveScore += score;
            } else {
                // 主观题：创建评卷任务，AI预评分
                GradeDetail detail = new GradeDetail();
                detail.setRecordId(recordId);
                detail.setQuestionId(questionId);
                detail.setStatus(0); // 待评
                double aiScore = aiPreScore(question, userAnswer);
                detail.setAiPreScore(aiScore);
                detail.setAiScoreBasis("{\"keywords\":\"AI预评结果\",\"score\":" + aiScore + "}");
                subjectiveDetails.add(detail);
            }
        }

        // 更新客观题得分
        record.setObjectiveScore(objectiveScore);
        if (subjectiveDetails.isEmpty()) {
            // 全是客观题，直接完成
            record.setTotalScore(objectiveScore);
            record.setStatus(3); // 已完成
        } else {
            // 有主观题，等待人工评卷
            record.setStatus(2); // 评卷中
            saveBatch(subjectiveDetails);
        }
        recordMapper.updateById(record);
        log.info("自动评卷完成，recordId: {}，客观题得分: {}", recordId, objectiveScore);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualGrade(Long detailId, Long graderId, Double score, String comment) {
        GradeDetail detail = getById(detailId);
        if (detail == null) {
            throw new BusinessException("评卷记录不存在");
        }

        if (detail.getGrader1Id() == null) {
            // 第一评卷员
            detail.setGrader1Id(graderId);
            detail.setScore1(score);
            detail.setComment1(comment);
            detail.setGradeTime1(LocalDateTime.now());
            detail.setStatus(0); // 等待第二评卷员
        } else if (detail.getGrader2Id() == null && !detail.getGrader1Id().equals(graderId)) {
            // 第二评卷员（不能是同一人）
            detail.setGrader2Id(graderId);
            detail.setScore2(score);
            detail.setComment2(comment);
            detail.setGradeTime2(LocalDateTime.now());

            // 判断分差是否超出阈值
            double diff = Math.abs(detail.getScore1() - score);
            if (diff > SCORE_DIFF_THRESHOLD) {
                detail.setStatus(2); // 待仲裁
            } else {
                // 取平均分
                detail.setFinalScore((detail.getScore1() + score) / 2.0);
                detail.setStatus(1); // 已完成
                updateRecordSubjectiveScore(detail.getRecordId());
            }
        }
        updateById(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arbitrateGrade(Long detailId, Long arbitratorId, Double score) {
        GradeDetail detail = getById(detailId);
        if (detail == null) {
            throw new BusinessException("评卷记录不存在");
        }
        detail.setArbitratorId(arbitratorId);
        detail.setArbitrateScore(score);
        detail.setArbitrateTime(LocalDateTime.now());
        detail.setFinalScore(score);
        detail.setStatus(3); // 已仲裁
        updateById(detail);
        updateRecordSubjectiveScore(detail.getRecordId());
    }

    @Override
    public Page<GradeDetail> pageGradingTasks(Integer pageNum, Integer pageSize, Long graderId, Integer status) {
        Page<GradeDetail> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GradeDetail> wrapper = new LambdaQueryWrapper<GradeDetail>()
                .eq(graderId != null, GradeDetail::getGrader1Id, graderId)
                .eq(status != null, GradeDetail::getStatus, status)
                .orderByAsc(GradeDetail::getCreateTime);
        return page(page, wrapper);
    }

    /**
     * 判断是否为客观题
     */
    private boolean isObjectiveQuestion(Integer type) {
        return type != null && type <= 4; // 1-单选 2-多选 3-判断 4-填空
    }

    /**
     * 客观题自动判分
     */
    private double judgeObjectiveAnswer(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) return 0;
        String correctAnswer = question.getAnswer();
        double fullScore = question.getScore() != null ? question.getScore() : 1.0;

        if (question.getType() == 1 || question.getType() == 3) {
            // 单选/判断：完全匹配
            return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim()) ? fullScore : 0;
        } else if (question.getType() == 2) {
            // 多选：全部正确才得分
            String[] userOpts = userAnswer.toUpperCase().replaceAll("[^A-Z]", "").split("");
            String[] correctOpts = correctAnswer.toUpperCase().replaceAll("[^A-Z]", "").split("");
            Arrays.sort(userOpts);
            Arrays.sort(correctOpts);
            return Arrays.equals(userOpts, correctOpts) ? fullScore : 0;
        } else if (question.getType() == 4) {
            // 填空：忽略大小写、前后空格
            String[] correctAnswers = correctAnswer.split("\\|");
            for (String ans : correctAnswers) {
                if (userAnswer.trim().equalsIgnoreCase(ans.trim())) return fullScore;
            }
            return 0;
        }
        return 0;
    }

    /**
     * 主观题AI智能预评分（基于关键词匹配）
     */
    private double aiPreScore(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) return 0;
        String referenceAnswer = question.getAnswer();
        double fullScore = question.getScore() != null ? question.getScore() : 10.0;

        // 简单关键词匹配算法（实际可替换为NLP模型）
        String[] keywords = referenceAnswer.split("[，,。？！\\s]+");
        int matchCount = 0;
        for (String kw : keywords) {
            if (kw.length() > 1 && userAnswer.contains(kw)) {
                matchCount++;
            }
        }
        double ratio = keywords.length > 0 ? (double) matchCount / keywords.length : 0;
        return Math.round(fullScore * ratio * 10) / 10.0;
    }

    /**
     * 更新答卷的主观题总分和最终总分
     */
    private void updateRecordSubjectiveScore(Long recordId) {
        List<GradeDetail> details = lambdaQuery()
                .eq(GradeDetail::getRecordId, recordId)
                .in(GradeDetail::getStatus, Arrays.asList(1, 3)) // 已完成或已仲裁
                .list();
        double subjectiveScore = details.stream()
                .mapToDouble(d -> d.getFinalScore() != null ? d.getFinalScore() : 0)
                .sum();

        ExamRecord record = recordMapper.selectById(recordId);
        if (record != null) {
            record.setSubjectiveScore(subjectiveScore);
            record.setTotalScore((record.getObjectiveScore() != null ? record.getObjectiveScore() : 0) + subjectiveScore);
            record.setStatus(3); // 已完成
            recordMapper.updateById(record);
        }
    }
}
