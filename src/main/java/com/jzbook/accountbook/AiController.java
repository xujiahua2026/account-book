package com.jzbook.accountbook;

import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.security.AuthInterceptor;
import com.jzbook.accountbook.service.AiService;
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
    public Map<String, Object> configCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("keyConfigured", aiService.isKeyConfigured());
        result.put("keyLength", aiService.getKeyLength());
        result.put("hasLeadingBom", aiService.hasLeadingBom());
        result.put("hasWhitespace", aiService.hasWhitespace());
        result.put("hasQuotes", aiService.hasQuotes());
        result.put("model", aiService.getModel());
        return result;
    }

    /**
     * 一句话记账
     * 请求头：Authorization: Bearer &lt;token&gt;
     * 请求体：{"text":"昨天午饭38块"}
     */
    @PostMapping("/record")
    public Map<String, Object> record(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId,
                                      @RequestBody Map<String, String> body) {
        Record r = aiService.parseAndSave(userId, body.get("text"));

        Map<String, Object> data = new HashMap<>();
        data.put("id", r.getId());
        data.put("type", r.getType());
        data.put("category", r.getCategory());
        data.put("amount", r.getAmount());
        data.put("recordDate", r.getRecordDate());
        data.put("remark", r.getRemark());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "已记账：" + r.getRemark());
        result.put("data", data);
        return result;
    }
}
