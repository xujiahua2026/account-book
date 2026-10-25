package com.jzbook.accountbook;

import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.service.RecordService;
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

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /** 记一笔 */
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Map<String, String> body) {
        Record r = new Record();
        r.setUserId(Long.parseLong(body.get("userId")));
        r.setType(Integer.parseInt(body.get("type")));
        r.setCategory(body.get("category"));
        r.setAmount(new BigDecimal(body.get("amount")));
        r.setRecordDate(LocalDate.parse(body.getOrDefault("recordDate", LocalDate.now().toString())));
        r.setRemark(body.get("remark"));

        recordService.add(r);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "记账成功");
        return result;
    }

    /** 查询我的全部记录 */
    @GetMapping("/list")
    public List<Record> list(@RequestParam Long userId) {
        return recordService.list(userId);
    }

    /** 删除一条（带归属校验） */
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody Map<String, String> body) {
        boolean ok = recordService.delete(
                Long.parseLong(body.get("id")),
                Long.parseLong(body.get("userId"))
        );

        Map<String, Object> result = new HashMap<>();
        result.put("code", ok ? 0 : 404);
        result.put("msg", ok ? "删除成功" : "记录不存在或无权删除");
        return result;
    }

    /** 修改一条（带归属校验） */
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody Map<String, String> body) {
        Record r = new Record();
        r.setId(Long.parseLong(body.get("id")));
        r.setUserId(Long.parseLong(body.get("userId")));
        r.setType(Integer.parseInt(body.get("type")));
        r.setCategory(body.get("category"));
        r.setAmount(new BigDecimal(body.get("amount")));
        r.setRecordDate(LocalDate.parse(body.get("recordDate")));
        r.setRemark(body.get("remark"));

        boolean ok = recordService.update(r);

        Map<String, Object> result = new HashMap<>();
        result.put("code", ok ? 0 : 404);
        result.put("msg", ok ? "修改成功" : "记录不存在或无权修改");
        return result;
    }

    /** 按月统计收入/支出 */
    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam Long userId, @RequestParam String month) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("month", month);
        result.put("data", recordService.summary(userId, month));
        return result;
    }
}
