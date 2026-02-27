package com.jepai.exam.modules.paper.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jepai.exam.modules.paper.entity.Paper;

/**
 * 试卷服务接口
 */
public interface PaperService extends IService<Paper> {

    Page<Paper> pagePapers(Integer pageNum, Integer pageSize, String keyword, Long orgId);

    void savePaper(Paper paper);

    void deletePaper(Long id);

    /**
     * 发布试卷
     */
    void publishPaper(Long id);
}
