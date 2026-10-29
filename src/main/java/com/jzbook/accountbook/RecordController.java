package com.jzbook.accountbook;

import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.security.AuthInterceptor;
import com.jzbook.accountbook.service.RecordService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记账接口
 * 注意：userId 全部来自 Token（@RequestAttribute），不再接收前端传入的 userId，
 * 从根源上避免「改个 userId 就能读写别人数据」的越权问题。
 */
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
    public Map<String, Object> add(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                                   @RequestBody Map<String, String> body) {
        Record r = new Record();
        r.setUserId(userId);
        r.setType(Integer.parseInt(body.get("type")));
        r.setCategory(body.get("category"));
        r.setAmount(new BigDecimal(body.get("amount")));
        r.setRecordDate(LocalDate.parse(body.getOrDefault("recordDate", LocalDate.now().toString())));
        r.setRemark(body.get("remark"));

        recordService.add(r);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "记账成功");
        result.put("id", r.getId());
        return result;
    }

    /** 查询我的全部记录 */
    @GetMapping("/list")
    public List<Record> list(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        return recordService.list(userId);
    }

    /** 删除一条（带归属校验） */
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                                      @RequestBody Map<String, String> body) {
        boolean ok = recordService.delete(Long.parseLong(body.get("id")), userId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", ok ? 0 : 404);
        result.put("msg", ok ? "删除成功" : "记录不存在或无权删除");
        return result;
    }

    /** 修改一条（带归属校验） */
    @PostMapping("/update")
    public Map<String, Object> update(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                                      @RequestBody Map<String, String> body) {
        Record r = new Record();
        r.setId(Long.parseLong(body.get("id")));
        r.setUserId(userId);
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
    public Map<String, Object> summary(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                                       @RequestParam String month) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("month", month);
        result.put("data", recordService.summary(userId, month));
        return result;
    }
}
