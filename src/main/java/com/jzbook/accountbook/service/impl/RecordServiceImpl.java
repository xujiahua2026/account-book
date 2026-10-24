package com.jzbook.accountbook.service.impl;

import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.mapper.RecordMapper;
import com.jzbook.accountbook.service.RecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;

    public RecordServiceImpl(RecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Override
    public void add(Record record) {
        if (record.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (record.getType() == null || (record.getType() != 1 && record.getType() != 2)) {
            throw new IllegalArgumentException("类型只能是1（支出）或2（收入）");
        }
        if (record.getCategory() == null || record.getCategory().isBlank()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        if (record.getAmount() == null || record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额必须大于0");
        }
        recordMapper.insert(record);
    }

    @Override
    public List<Record> list(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return recordMapper.listByUserId(userId);
    }

    @Override
    public boolean delete(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        // SQL 中同时匹配 id 和 user_id，实现数据权限隔离
        return recordMapper.deleteById(id, userId) > 0;
    }

    @Override
    public Map<String, Object> summary(Long userId, String month) {
        if (userId == null || month == null || month.isBlank()) {
            throw new IllegalArgumentException("用户ID和月份不能为空");
        }
        return recordMapper.sumByMonth(userId, month);
    }
}
