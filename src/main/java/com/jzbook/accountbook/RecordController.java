package com.jzbook.accountbook;

import com.jzbook.accountbook.common.Result;
import com.jzbook.accountbook.dto.RecordRequest;
import com.jzbook.accountbook.dto.RecordUpdateRequest;
import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.security.AuthInterceptor;
import com.jzbook.accountbook.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 记账接口
 * 1. userId 全部来自 Token（@RequestAttribute），不接收前端传入，避免越权
 * 2. 参数校验由 @Valid + DTO 注解完成，错误统一由全局异常处理器返回 400
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
    public Result<Map<String, Object>> add(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                                           @Valid @RequestBody RecordRequest req) {
        Record r = toEntity(req);
        r.setUserId(userId);
        recordService.add(r);
        return Result.ok("记账成功", Map.of("id", r.getId()));
    }

    /** 查询我的全部记录 */
    @GetMapping("/list")
    public Result<List<Record>> list(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
        return Result.ok(recordService.list(userId));
    }

    /** 删除一条（带归属校验） */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                               @RequestBody Map<String, String> body) {
        Long id = parseId(body.get("id"));
        boolean ok = recordService.delete(id, userId);
        return ok ? Result.ok("删除成功", null) : Result.fail(404, "记录不存在或无权删除");
    }

    /** 修改一条（带归属校验） */
    @PostMapping("/update")
    public Result<Void> update(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                               @Valid @RequestBody RecordUpdateRequest req) {
        Record r = toEntity(req);
        r.setId(req.getId());
        r.setUserId(userId);
        boolean ok = recordService.update(r);
        return ok ? Result.ok("修改成功", null) : Result.fail(404, "记录不存在或无权修改");
    }

    /** 按月统计收入/支出 */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                                               @RequestParam String month) {
        if (!month.matches("\\d{4}-\\d{2}")) {
            return Result.fail(400, "month 格式应为 yyyy-MM");
        }
        return Result.ok(recordService.summary(userId, month));
    }

    private Record toEntity(RecordRequest req) {
        Record r = new Record();
        r.setType(req.getType());
        r.setCategory(req.getCategory().trim());
        r.setAmount(req.getAmount());
        r.setRecordDate(req.getRecordDate() == null ? LocalDate.now() : req.getRecordDate());
        r.setRemark(req.getRemark());
        return r;
    }

    private Long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("id 格式不正确");
        }
    }
}
