package com.jepai.exam.modules.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jepai.exam.common.exception.BusinessException;
import com.jepai.exam.common.utils.SecurityUtils;
import com.jepai.exam.modules.paper.entity.Paper;
import com.jepai.exam.modules.paper.mapper.PaperMapper;
import com.jepai.exam.modules.paper.service.PaperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 试卷服务实现
 */
@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    @Override
    public Page<Paper> pagePapers(Integer pageNum, Integer pageSize, String keyword, Long orgId) {
        Page<Paper> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<Paper>()
                .like(StringUtils.hasText(keyword), Paper::getName, keyword)
                .eq(orgId != null, Paper::getOrgId, orgId)
                .orderByDesc(Paper::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePaper(Paper paper) {
        if (paper.getId() == null) {
            paper.setCreatorId(SecurityUtils.getCurrentUserId());
            paper.setStatus(0);
            save(paper);
        } else {
            updateById(paper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePaper(Long id) {
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPaper(Long id) {
        Paper paper = getById(id);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        lambdaUpdate().eq(Paper::getId, id).set(Paper::getStatus, 1).update();
    }
}
