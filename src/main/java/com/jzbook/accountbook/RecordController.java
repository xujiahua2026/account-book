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
        Map<String, Object> result = new HashMap<>();
        try {
            Record r = new Record();
            r.setUserId(Long.parseLong(body.get("userId")));
            r.setType(Integer.parseInt(body.get("type")));
            r.setCategory(body.get("category"));
            r.setAmount(new BigDecimal(body.get("amount")));
            r.setRecordDate(LocalDate.parse(body.getOrDefault("recordDate", LocalDate.now().toString())));
            r.setRemark(body.get("remark"));

            recordService.add(r);
            result.put("code", 0);
            result.put("msg", "记账成功");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 400);
            result.put("msg", "参数格式有误，请检查金额和日期");
        }
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
        Map<String, Object> result = new HashMap<>();
        try {
            boolean ok = recordService.delete(
                    Long.parseLong(body.get("id")),
                    Long.parseLong(body.get("userId"))
            );
            result.put("code", ok ? 0 : 404);
            result.put("msg", ok ? "删除成功" : "记录不存在或无权删除");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /** 按月统计收入/支出 */
    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam Long userId, @RequestParam String month) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("code", 0);
            result.put("month", month);
            result.put("data", recordService.summary(userId, month));
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
