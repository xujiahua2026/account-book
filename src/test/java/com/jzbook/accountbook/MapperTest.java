package com.jzbook.accountbook;

import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.entity.User;
import com.jzbook.accountbook.mapper.RecordMapper;
import com.jzbook.accountbook.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional   // 每个测试结束后自动回滚，不污染数据库
class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RecordMapper recordMapper;

    @Test
    void insertThenFindByUsername() {
        User u = new User();
        u.setUsername("test_junit_" + System.currentTimeMillis());
        u.setPassword("e10adc3949ba59abbe56e057f20f883e");
        u.setNickname("单元测试用户");

        int rows = userMapper.insert(u);
        assertEquals(1, rows, "插入应影响1行");

        User found = userMapper.findByUsername(u.getUsername());
        assertNotNull(found, "应能按用户名查到刚插入的用户");
        assertEquals("单元测试用户", found.getNickname());
    }

    @Test
    void findByUsernameReturnsNullWhenNotExist() {
        assertNull(userMapper.findByUsername("no_such_user_99999"));
    }

    @Test
    void insertRecordThenListAndSummary() {
        Long userId = 2L;

        Record r = new Record();
        r.setUserId(userId);
        r.setType(1);
        r.setCategory("餐饮");
        r.setAmount(new BigDecimal("66.60"));
        r.setRecordDate(LocalDate.now());
        r.setRemark("junit测试");

        assertEquals(1, recordMapper.insert(r));

        List<Record> list = recordMapper.listByUserId(userId);
        assertFalse(list.isEmpty(), "列表不应为空");
        assertTrue(list.stream().anyMatch(x -> "junit测试".equals(x.getRemark())),
                "列表中应包含刚插入的记录");

        String month = LocalDate.now().toString().substring(0, 7);
        Map<String, Object> sum = recordMapper.sumByMonth(userId, month);
        assertNotNull(sum);
        assertNotNull(sum.get("expense"), "统计结果应包含支出金额");
    }

    @Test
    void deleteOthersRecordShouldFail() {
        Long ownerId = 2L;
        Long otherId = 999L;

        Record r = new Record();
        r.setUserId(ownerId);
        r.setType(1);
        r.setCategory("测试");
        r.setAmount(new BigDecimal("1.00"));
        r.setRecordDate(LocalDate.now());
        recordMapper.insert(r);

        Long newId = recordMapper.listByUserId(ownerId).get(0).getId();
        assertEquals(0, recordMapper.deleteById(newId, otherId), "他人无权删除，应影响0行");
        assertEquals(1, recordMapper.deleteById(newId, ownerId), "本人可以删除");
    }
}
