package com.jepai.exam.modules.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jepai.exam.common.config.RabbitMQConfig;
import com.jepai.exam.common.exception.BusinessException;
import com.jepai.exam.common.utils.SecurityUtils;
import com.jepai.exam.modules.exam.dto.AnswerSubmitDTO;
import com.jepai.exam.modules.exam.entity.ExamArrange;
import com.jepai.exam.modules.exam.entity.ExamRecord;
import com.jepai.exam.modules.exam.mapper.ExamArrangeMapper;
import com.jepai.exam.modules.exam.mapper.ExamRecordMapper;
import com.jepai.exam.modules.exam.service.ExamService;
import com.jepai.exam.modules.grade.service.GradeService;
import com.jepai.exam.modules.paper.entity.Paper;
import com.jepai.exam.modules.paper.service.PaperService;
import com.jepai.exam.modules.question.entity.Question;
import com.jepai.exam.modules.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 考试服务实现
 * 核心特色：Redis实时缓存答题进度、断网续答高可用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl extends ServiceImpl<ExamArrangeMapper, ExamArrange> implements ExamService {

    private final ExamRecordMapper recordMapper;
    private final PaperService paperService;
    private final QuestionService questionService;
    private final RedisTemplate<String, Object> redisTemplate;
    @Nullable
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    private final GradeService gradeService;
    private final ObjectMapper objectMapper;

    @Value("${exam.rabbitmq.enabled:false}")
    private boolean rabbitMqEnabled;

    /** Redis key 前缀：考生答题进度 */
    private static final String ANSWER_CACHE_KEY = "exam:answer:%d:%d";
    /** Redis 缓存过期时间：8小时 */
    private static final long CACHE_EXPIRE_HOURS = 8;

    @Override
    public Page<ExamArrange> pageExams(Integer pageNum, Integer pageSize, String keyword, Long orgId, Integer status) {
        Page<ExamArrange> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamArrange> wrapper = new LambdaQueryWrapper<ExamArrange>()
                .like(StringUtils.hasText(keyword), ExamArrange::getName, keyword)
                .eq(orgId != null, ExamArrange::getOrgId, orgId)
                .eq(status != null, ExamArrange::getStatus, status)
                .orderByDesc(ExamArrange::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveExam(ExamArrange exam) {
        if (exam.getId() == null) {
            exam.setCreatorId(SecurityUtils.getCurrentUserId());
            exam.setStatus(0);
            save(exam);
        } else {
            updateById(exam);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishExam(Long examId) {
        lambdaUpdate().eq(ExamArrange::getId, examId).set(ExamArrange::getStatus, 1).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endExam(Long examId) {
        lambdaUpdate().eq(ExamArrange::getId, examId).set(ExamArrange::getStatus, 3).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> enterExam(Long examId, Long userId) {
        ExamArrange exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        if (exam.getStatus() != 1 && exam.getStatus() != 2) {
            throw new BusinessException("考试未开始或已结束");
        }

        // 查找已有的进行中答卷
        ExamRecord record = recordMapper.selectOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getExamId, examId)
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getStatus, 0));

        if (record == null) {
            // 首次进入，创建答卷记录
            record = new ExamRecord();
            record.setExamId(examId);
            record.setUserId(userId);
            record.setStatus(0);
            record.setStartTime(LocalDateTime.now());
            record.setSwitchCount(0);
            recordMapper.insert(record);
        }

        // 从Redis恢复答题进度
        String cacheKey = String.format(ANSWER_CACHE_KEY, record.getId(), userId);
        Object cachedAnswers = null;
        try {
            cachedAnswers = redisTemplate.opsForValue().get(cacheKey);
        } catch (DataAccessException e) {
            log.warn("Redis不可用，无法恢复答题进度", e);
        }

        // 获取试卷内容，并填充题目详情
        Paper paper = paperService.getById(exam.getPaperId());

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("examInfo", exam);
        result.put("paperContent", paper != null ? buildExamQuestions(paper.getContent()) : null);
        result.put("savedAnswers", cachedAnswers);
        result.put("startTime", record.getStartTime());
        return result;
    }

    /**
     * 解析试卷内容，从题库中获取题目详情，返回供考生答题的题目列表（不含答案）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildExamQuestions(String paperContentJson) {
        if (paperContentJson == null || paperContentJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> paperContent = objectMapper.readValue(
                    paperContentJson, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> sections =
                    (List<Map<String, Object>>) paperContent.get("sections");
            if (sections == null) {
                return Collections.emptyList();
            }
            List<Map<String, Object>> questions = new ArrayList<>();
            for (Map<String, Object> section : sections) {
                List<Map<String, Object>> sectionQuestions =
                        (List<Map<String, Object>>) section.get("questions");
                if (sectionQuestions == null) continue;
                for (Map<String, Object> qRef : sectionQuestions) {
                    Object questionIdObj = qRef.get("questionId");
                    if (questionIdObj == null) {
                        log.warn("试卷内容中存在无效的题目引用（questionId 为空）");
                        continue;
                    }
                    Long questionId = ((Number) questionIdObj).longValue();
                    Double score = qRef.get("score") != null
                            ? ((Number) qRef.get("score")).doubleValue() : null;
                    Question question = questionService.getById(questionId);
                    if (question == null) {
                        log.warn("试卷引用的题目不存在，questionId: {}", questionId);
                        continue;
                    }
                    Map<String, Object> qMap = new HashMap<>();
                    qMap.put("id", question.getId());
                    qMap.put("content", question.getContent());
                    qMap.put("type", question.getType());
                    qMap.put("options", question.getOptions());
                    qMap.put("score", score != null ? score : question.getScore());
                    questions.add(qMap);
                }
            }
            return questions;
        } catch (Exception e) {
            log.error("解析试卷内容失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void saveAnswerProgress(AnswerSubmitDTO dto) {
        // 实时将答题进度保存到Redis
        String cacheKey = String.format(ANSWER_CACHE_KEY, dto.getRecordId(), SecurityUtils.getCurrentUserId());
        try {
            redisTemplate.opsForValue().set(cacheKey, dto.getAnswers(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (DataAccessException e) {
            log.warn("Redis不可用，答题进度保存失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamRecord submitExam(Long recordId, Long userId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答卷记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该答卷已提交");
        }

        // 从Redis获取最终答案
        String cacheKey = String.format(ANSWER_CACHE_KEY, recordId, userId);
        Object cachedAnswers = null;
        try {
            cachedAnswers = redisTemplate.opsForValue().get(cacheKey);
        } catch (DataAccessException e) {
            log.warn("Redis不可用，无法获取缓存答案", e);
        }

        record.setStatus(1);
        record.setSubmitTime(LocalDateTime.now());
        if (cachedAnswers != null) {
            try {
                record.setAnswers(objectMapper.writeValueAsString(cachedAnswers));
            } catch (Exception e) {
                log.error("序列化答案失败", e);
            }
        }
        if (record.getStartTime() != null) {
            long usedSeconds = java.time.Duration.between(record.getStartTime(), record.getSubmitTime()).getSeconds();
            record.setUsedTime((int) usedSeconds);
        }
        recordMapper.updateById(record);

        // 发送到RabbitMQ异步评卷（RabbitMQ未启用时降级为同步评卷）
        if (rabbitMqEnabled && rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.GRADING_EXCHANGE,
                        RabbitMQConfig.GRADING_ROUTING_KEY,
                        recordId
                );
            } catch (AmqpException e) {
                log.warn("RabbitMQ不可用，降级为同步评卷, recordId: {}", recordId, e);
                gradeService.autoGrade(recordId);
            }
        } else {
            log.info("RabbitMQ未启用，使用同步评卷, recordId: {}", recordId);
            gradeService.autoGrade(recordId);
        }

        // 清除Redis缓存
        try {
            redisTemplate.delete(cacheKey);
        } catch (DataAccessException e) {
            log.warn("Redis不可用，无法清除缓存", e);
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceSubmit(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record != null && record.getStatus() == 0) {
            submitExam(recordId, record.getUserId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportSwitchScreen(Long recordId, Long userId) {
        // 更新切屏次数
        ExamRecord record = recordMapper.selectById(recordId);
        if (record != null) {
            record.setSwitchCount(record.getSwitchCount() + 1);
            recordMapper.updateById(record);

            // 检查是否超过切屏限制
            ExamArrange exam = getById(record.getExamId());
            if (exam != null && exam.getSwitchScreenLimit() > 0
                    && record.getSwitchCount() >= exam.getSwitchScreenLimit()) {
                // 强制交卷
                submitExam(recordId, userId);
                log.info("考生 {} 切屏次数超限，已强制交卷", userId);
            }
        }
    }

    @Override
    public Page<ExamRecord> pageRecords(Integer pageNum, Integer pageSize, Long examId, Long userId) {
        Page<ExamRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<ExamRecord>()
                .eq(examId != null, ExamRecord::getExamId, examId)
                .eq(userId != null, ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getCreateTime);
        return recordMapper.selectPage(page, wrapper);
    }
}
