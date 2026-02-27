package com.jepai.exam.modules.question.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jepai.exam.modules.question.dto.QuestionQueryDTO;
import com.jepai.exam.modules.question.dto.QuestionSaveDTO;
import com.jepai.exam.modules.question.entity.Question;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 题目服务接口
 */
public interface QuestionService extends IService<Question> {

    Page<Question> pageQuestions(QuestionQueryDTO query);

    void saveQuestion(QuestionSaveDTO dto);

    void deleteQuestion(Long id);

    void auditQuestion(Long id, Integer status, String remark);

    /**
     * Excel批量导入题目
     */
    int importFromExcel(MultipartFile file, Long orgId, Long subjectId);

    /**
     * 随机抽题（用于随机组卷）
     */
    List<Question> randomSelect(Long subjectId, Integer type, Integer difficulty, Integer count, List<Long> excludeIds);
}
