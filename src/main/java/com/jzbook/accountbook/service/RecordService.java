package com.jzbook.accountbook.service;

import com.jzbook.accountbook.entity.Record;

import java.util.List;
import java.util.Map;

/**
 * 记账业务接口
 */
public interface RecordService {

    /** 新增一笔记录 */
    void add(Record record);

    /** 查询某个用户的全部记录（按日期倒序） */
    List<Record> list(Long userId);

    /** 删除记录，返回是否删除成功（含归属校验） */
    boolean delete(Long id, Long userId);

    /** 按月统计收入和支出 */
    Map<String, Object> summary(Long userId, String month);
}
