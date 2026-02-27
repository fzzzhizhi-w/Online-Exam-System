package com.jepai.exam.modules.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jepai.exam.common.exception.BusinessException;
import com.jepai.exam.common.utils.SecurityUtils;
import com.jepai.exam.modules.question.dto.QuestionQueryDTO;
import com.jepai.exam.modules.question.dto.QuestionSaveDTO;
import com.jepai.exam.modules.question.entity.Question;
import com.jepai.exam.modules.question.mapper.QuestionMapper;
import com.jepai.exam.modules.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 题目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Override
    public Page<Question> pageQuestions(QuestionQueryDTO query) {
        Page<Question> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .like(StringUtils.hasText(query.getKeyword()), Question::getContent, query.getKeyword())
                .eq(query.getType() != null, Question::getType, query.getType())
                .eq(query.getSubjectId() != null, Question::getSubjectId, query.getSubjectId())
                .eq(query.getDifficulty() != null, Question::getDifficulty, query.getDifficulty())
                .eq(query.getAuditStatus() != null, Question::getAuditStatus, query.getAuditStatus())
                .eq(query.getStatus() != null, Question::getStatus, query.getStatus())
                .eq(query.getOrgId() != null, Question::getOrgId, query.getOrgId())
                .orderByDesc(Question::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestion(QuestionSaveDTO dto) {
        if (dto.getId() == null) {
            Question question = new Question();
            copyDtoToEntity(dto, question);
            question.setCreatorId(SecurityUtils.getCurrentUserId());
            question.setAuditStatus(0);
            question.setStatus(0);
            question.setUsedCount(0);
            save(question);
        } else {
            Question question = getById(dto.getId());
            if (question == null) {
                throw new BusinessException("题目不存在");
            }
            copyDtoToEntity(dto, question);
            updateById(question);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditQuestion(Long id, Integer status, String remark) {
        lambdaUpdate()
                .eq(Question::getId, id)
                .set(Question::getAuditStatus, status)
                .set(Question::getAuditRemark, remark)
                .set(Question::getAuditorId, SecurityUtils.getCurrentUserId())
                .set(Question::getAuditTime, LocalDateTime.now())
                .set(status == 1, Question::getStatus, 1)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importFromExcel(MultipartFile file, Long orgId, Long subjectId) {
        List<Question> questions = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Question q = parseRowToQuestion(row, orgId, subjectId);
                if (q != null) {
                    questions.add(q);
                }
            }
        } catch (Exception e) {
            log.error("Excel导入题目失败", e);
            throw new BusinessException("Excel文件解析失败: " + e.getMessage());
        }
        if (!questions.isEmpty()) {
            saveBatch(questions);
        }
        return questions.size();
    }

    @Override
    public List<Question> randomSelect(Long subjectId, Integer type, Integer difficulty, Integer count, List<Long> excludeIds) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getSubjectId, subjectId)
                .eq(Question::getStatus, 1)
                .eq(Question::getAuditStatus, 1)
                .eq(type != null, Question::getType, type)
                .eq(difficulty != null, Question::getDifficulty, difficulty)
                .notIn(excludeIds != null && !excludeIds.isEmpty(), Question::getId, excludeIds);
        List<Question> all = list(wrapper);
        Collections.shuffle(all);
        return all.size() > count ? all.subList(0, count) : all;
    }

    private Question parseRowToQuestion(Row row, Long orgId, Long subjectId) {
        try {
            Question q = new Question();
            q.setContent(getCellValue(row, 0));
            q.setType(Integer.parseInt(getCellValue(row, 1)));
            q.setOptions(getCellValue(row, 2));
            q.setAnswer(getCellValue(row, 3));
            q.setAnalysis(getCellValue(row, 4));
            q.setDifficulty(Integer.parseInt(getCellValue(row, 5)));
            q.setScore(Double.parseDouble(getCellValue(row, 6)));
            q.setKnowledgeTags(getCellValue(row, 7));
            q.setSubjectId(subjectId);
            q.setOrgId(orgId);
            q.setAuditStatus(0);
            q.setStatus(0);
            q.setUsedCount(0);
            q.setCreatorId(SecurityUtils.getCurrentUserId());
            return q;
        } catch (Exception e) {
            log.warn("解析行失败，跳过: {}", e.getMessage());
            return null;
        }
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private void copyDtoToEntity(QuestionSaveDTO dto, Question question) {
        question.setContent(dto.getContent());
        question.setType(dto.getType());
        question.setOptions(dto.getOptions());
        question.setAnswer(dto.getAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setSubjectId(dto.getSubjectId());
        question.setKnowledgeTags(dto.getKnowledgeTags());
        question.setDifficulty(dto.getDifficulty());
        question.setScore(dto.getScore());
        question.setOrgId(dto.getOrgId());
    }
}
