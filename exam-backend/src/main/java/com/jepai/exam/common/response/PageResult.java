package com.jepai.exam.common.response;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页响应结果封装
 */
@Data
public class PageResult<T> {
    /** 总记录数 */
    private Long total;
    /** 总页数 */
    private Long pages;
    /** 当前页 */
    private Long current;
    /** 每页大小 */
    private Long size;
    /** 数据列表 */
    private List<T> records;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(page.getRecords());
        return result;
    }
}
