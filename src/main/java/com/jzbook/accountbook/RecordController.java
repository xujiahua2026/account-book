package com.jzbook.accountbook;

import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.mapper.RecordMapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/record")
public class RecordController {

    private final RecordMapper recordMapper;

    public RecordController(RecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    // 记一笔
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            Record r = new Record();
            r.setUserId(Long.parseLong(body.get("userId")));
            r.setType(Integer.parseInt(body.get("type")));
            r.setCategory(body.get("category"));
            r.setAmount(new BigDecimal(body.get("amount")));
            r.setRecordDate(LocalDate.parse(body.getOrDefault("recordDate", LocalDate.now().toString())));
            r.setRemark(body.get("remark"));
            recordMapper.insert(r);
            result.put("code", 0);
            result.put("msg", "记账成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("msg", "参数有误：" + e.getMessage());
        }
        return result;
    }

    // 查我的所有记录
    @GetMapping("/list")
    public List<Record> list(@RequestParam Long userId) {
        return recordMapper.listByUserId(userId);
    }

    // 删一条
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        int rows = recordMapper.deleteById(Long.parseLong(body.get("id")), Long.parseLong(body.get("userId")));
        result.put("code", rows > 0 ? 0 : 404);
        result.put("msg", rows > 0 ? "删除成功" : "记录不存在或无权删除");
        return result;
    }
    // 按月统计收入/支出
    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam Long userId, @RequestParam String month) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> sum = recordMapper.sumByMonth(userId, month);
        result.put("code", 0);
        result.put("month", month);
        result.put("data", sum);
        return result;
    }

}

