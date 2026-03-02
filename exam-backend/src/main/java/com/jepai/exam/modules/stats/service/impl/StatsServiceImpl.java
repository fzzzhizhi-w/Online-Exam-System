package com.jepai.exam.modules.stats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jepai.exam.modules.exam.entity.ExamArrange;
import com.jepai.exam.modules.exam.entity.ExamRecord;
import com.jepai.exam.modules.exam.mapper.ExamArrangeMapper;
import com.jepai.exam.modules.exam.mapper.ExamRecordMapper;
import com.jepai.exam.modules.question.entity.Question;
import com.jepai.exam.modules.question.mapper.QuestionMapper;
import com.jepai.exam.modules.stats.service.StatsService;
import com.jepai.exam.modules.user.entity.SysUser;
import com.jepai.exam.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务实现
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ExamRecordMapper recordMapper;
    private final ExamArrangeMapper examMapper;
    private final SysUserMapper userMapper;
    private final QuestionMapper questionMapper;

    @Override
    public Map<String, Object> getExamOverview(Long examId) {
        List<ExamRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStatus, 3));

        ExamArrange exam = examMapper.selectById(examId);
        long total = records.size();
        if (total == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("avgScore", 0);
            result.put("passRate", 0);
            result.put("maxScore", 0);
            result.put("minScore", 0);
            return result;
        }

        DoubleSummaryStatistics stats = records.stream()
                .filter(r -> r.getTotalScore() != null)
                .mapToDouble(ExamRecord::getTotalScore)
                .summaryStatistics();

        // 获取及格分
        long passed = records.stream()
                .filter(r -> r.getPassed() != null && r.getPassed() == 1)
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("examName", exam != null ? exam.getName() : "");
        result.put("total", total);
        result.put("avgScore", Math.round(stats.getAverage() * 10.0) / 10.0);
        result.put("maxScore", stats.getMax());
        result.put("minScore", stats.getMin());
        result.put("passCount", passed);
        result.put("passRate", Math.round((double) passed / total * 100 * 10.0) / 10.0);
        return result;
    }

    @Override
    public Map<String, Object> getScoreDistribution(Long examId) {
        List<ExamRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStatus, 3)
                        .isNotNull(ExamRecord::getTotalScore));

        // 分数段：0-59, 60-69, 70-79, 80-89, 90-100
        int[] ranges = {0, 60, 70, 80, 90, 101};
        String[] labels = {"0-59", "60-69", "70-79", "80-89", "90-100"};
        int[] counts = new int[5];

        for (ExamRecord record : records) {
            double score = record.getTotalScore();
            for (int i = 0; i < ranges.length - 1; i++) {
                if (score >= ranges[i] && score < ranges[i + 1]) {
                    counts[i]++;
                    break;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("counts", counts);
        return result;
    }

    @Override
    public Map<String, Object> getKnowledgeMastery(Long examId) {
        // 返回知识点掌握度数据（简化实现）
        Map<String, Object> result = new HashMap<>();
        result.put("message", "知识点掌握度分析需结合题目知识点标签计算");
        return result;
    }

    @Override
    public Map<String, Object> getPersonalReport(Long recordId, Long userId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "成绩记录不存在");
            return result;
        }

        ExamArrange exam = examMapper.selectById(record.getExamId());
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("examName", exam != null ? exam.getName() : "");
        result.put("totalScore", record.getTotalScore());
        result.put("objectiveScore", record.getObjectiveScore());
        result.put("subjectiveScore", record.getSubjectiveScore());
        result.put("passed", record.getPassed());
        result.put("usedTime", record.getUsedTime());
        return result;
    }

    @Override
    public Map<String, Object> getMyHistory(Long userId, Integer pageNum, Integer pageSize) {
        Page<ExamRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getCreateTime);
        Page<ExamRecord> result = recordMapper.selectPage(page, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("records", result.getRecords());
        return map;
    }

    @Override
    public Map<String, Object> getDashboard() {
        long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0));
        long questionCount = questionMapper.selectCount(null);
        long examCount = examMapper.selectCount(null);
        long recordCount = recordMapper.selectCount(null);

        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userCount);
        result.put("questionCount", questionCount);
        result.put("examCount", examCount);
        result.put("recordCount", recordCount);
        return result;
    }

    @Override
    public Map<String, Object> getStudentDashboard(Long userId) {
        long pendingExamCount = examMapper.selectCount(
                new LambdaQueryWrapper<ExamArrange>().in(ExamArrange::getStatus, 1, 2));
        long completedExamCount = recordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getStatus, 3));

        List<ExamRecord> completedRecords = recordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getStatus, 3)
                        .isNotNull(ExamRecord::getTotalScore));

        double avgScore = completedRecords.stream()
                .mapToDouble(ExamRecord::getTotalScore)
                .average()
                .orElse(0.0);
        double maxScore = completedRecords.stream()
                .mapToDouble(ExamRecord::getTotalScore)
                .max()
                .orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("pendingExamCount", pendingExamCount);
        result.put("completedExamCount", completedExamCount);
        result.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
        result.put("maxScore", maxScore);
        return result;
    }
}
