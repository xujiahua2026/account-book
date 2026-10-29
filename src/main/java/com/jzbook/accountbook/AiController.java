package com.jzbook.accountbook;

import com.jzbook.accountbook.common.Result;
import com.jzbook.accountbook.dto.AiTextRequest;
import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.security.AuthInterceptor;
import com.jzbook.accountbook.service.AiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 记账接口：一句话记账
 */
@CrossOrigin
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 配置自检（不返回密钥内容，只返回长度与格式特征），仅用于排查 401
     */
    @GetMapping("/config-check")
    public Result<Map<String, Object>> configCheck() {
        Map<String, Object> data = new HashMap<>();
        data.put("keyConfigured", aiService.isKeyConfigured());
        data.put("keyLength", aiService.getKeyLength());
        data.put("hasLeadingBom", aiService.hasLeadingBom());
        data.put("hasWhitespace", aiService.hasWhitespace());
        data.put("hasQuotes", aiService.hasQuotes());
        data.put("model", aiService.getModel());
        return Result.ok(data);
    }

    /**
     * 一句话记账
     * 请求头：Authorization: Bearer &lt;token&gt;
     * 请求体：{"text":"昨天午饭38块"}
     */
    @PostMapping("/record")
    public Result<Map<String, Object>> record(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                                              @Valid @RequestBody AiTextRequest req) {
        Record r = aiService.parseAndSave(userId, req.getText());

        Map<String, Object> data = new HashMap<>();
        data.put("id", r.getId());
        data.put("type", r.getType());
        data.put("category", r.getCategory());
        data.put("amount", r.getAmount());
        data.put("recordDate", r.getRecordDate());
        data.put("remark", r.getRemark());

        return Result.ok("已记账：" + r.getRemark(), data);
    }
}
