package com.jzbook.accountbook.service.impl;

import com.jzbook.accountbook.entity.Record;
import com.jzbook.accountbook.service.AiService;
import com.jzbook.accountbook.service.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

/**
 * 调用大模型（OpenAI 兼容协议）把自然语言转成记账记录
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final RecordService recordService;
    private final ObjectMapper mapper = JsonMapper.builder().build();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String baseUrl;

    @Value("${ai.model:glm-4-flash}")
    private String model;

    public AiServiceImpl(RecordService recordService) {
        this.recordService = recordService;
    }

    @Override
    public Record parseAndSave(Long userId, String text) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("请输入一句话，例如：昨天午饭38块");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("尚未配置大模型密钥 ai.api-key");
        }

        String content = callModel(text);
        Record record = parseRecord(content);
        record.setUserId(userId);
        recordService.add(record);
        return record;
    }

    @Override
    public boolean isKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public int getKeyLength() {
        return apiKey == null ? 0 : apiKey.trim().length();
    }

    @Override
    public boolean hasLeadingBom() {
        return apiKey != null && !apiKey.isEmpty() && apiKey.charAt(0) == '\uFEFF';
    }

    @Override
    public boolean hasWhitespace() {
        if (apiKey == null) {
            return false;
        }
        // 内部（非首尾）出现空白字符即为异常
        return !apiKey.equals(apiKey.trim()) || apiKey.matches(".*\\s.*");
    }

    @Override
    public boolean hasQuotes() {
        return apiKey != null && (apiKey.startsWith("\"") || apiKey.endsWith("\""));
    }

    @Override
    public String getModel() {
        return model;
    }

    /** 组装 Prompt 并调用大模型接口 */
    private String callModel(String text) {
        try {
            String systemPrompt = """
                    你是记账助手。把用户的一句话解析成一条记账记录，只输出 JSON，不要任何解释。
                    输出格式（字段必须全部存在）：
                    {"type":1,"category":"餐饮","amount":38.00,"dayOffset":-1,"remark":"午饭"}
                    规则：
                    1. type：1 表示支出，2 表示收入。
                    2. category 只能从这些里选：餐饮、交通、购物、居住、娱乐、医疗、其他（支出）；工资、兼职、理财、红包、其他（收入）。
                    3. amount 是正数，不带货币符号。
                    4. dayOffset 是相对今天的天数偏移：今天=0，昨天=-1，前天=-2，"上周五"等更早的说法按实际相差天数给负数。不要输出具体日期，具体日期由系统换算，你只负责数天数。
                    5. remark 用不超过 10 个字概括这笔账。
                    6. 如果用户说的不是记账内容（比如打招呼、问问题），输出 {"error":"不是记账内容"}。
                    """;

            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", 0.1);

            ArrayNode messages = body.putArray("messages");
            ObjectNode sys = messages.addObject();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            ObjectNode usr = messages.addObject();
            usr.put("role", "user");
            usr.put("content", text);

            ObjectNode respFormat = body.putObject("response_format");
            respFormat.put("type", "json_object");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + sanitizeKey(apiKey))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            mapper.writeValueAsString(body), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                log.error("大模型调用失败 status={} body={}", response.statusCode(), response.body());
                throw new IllegalStateException("AI 服务暂时不可用（HTTP " + response.statusCode() + "）");
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode()) {
                throw new IllegalStateException("AI 返回内容异常");
            }
            return contentNode.asText();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用大模型异常", e);
            throw new IllegalStateException("AI 服务调用失败：" + e.getMessage());
        }
    }

    /** 把模型返回的文本解析成 Record，并做二次校验（不信任模型输出） */
    private Record parseRecord(String raw) {
        String json = stripCodeFence(raw);
        try {
            JsonNode node = mapper.readTree(json);

            if (node.hasNonNull("error")) {
                throw new IllegalArgumentException("这句话里没听出记账信息，可以试试说：昨天午饭38块");
            }

            int type = node.path("type").asInt(0);
            if (type != 1 && type != 2) {
                throw new IllegalArgumentException("没识别出是支出还是收入，请说得更具体些");
            }
            String category = node.path("category").asText("").trim();
            if (category.isEmpty()) {
                category = "其他";
            }
            BigDecimal amount = new BigDecimal(node.path("amount").asText("0").replace(",", "").trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("没识别出金额，请再说一次");
            }
            // 日期：优先用模型给的 dayOffset（相对天数，由代码换算，避免模型算错日期），
            // 兼容旧的 recordDate 字段；最后兜底校验不能是未来日期
            LocalDate today = LocalDate.now();
            LocalDate date;
            if (node.has("dayOffset")) {
                int offset = node.path("dayOffset").asInt(0);
                if (offset > 0) {
                    offset = 0;
                }
                if (offset < -365) {
                    offset = -365;
                }
                date = today.plusDays(offset);
            } else {
                String dateText = node.path("recordDate").asText("").trim();
                date = dateText.isEmpty() ? today : LocalDate.parse(dateText);
            }
            if (date.isAfter(today)) {
                date = today;
            }

            Record r = new Record();
            r.setType(type);
            r.setCategory(category);
            r.setAmount(amount);
            r.setRecordDate(date);
            r.setRemark(node.path("remark").asText("").trim());
            return r;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("模型输出无法解析: {}", raw);
            throw new IllegalArgumentException("AI 没理解这句话，换个说法试试");
        }
    }

    /** 清理密钥中的 BOM、空白和引号（配置文件手工粘贴常见问题） */
    private String sanitizeKey(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.replace("\uFEFF", "").trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 1) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    /** 去掉模型可能带上的 ```json 代码块标记 */
    private String stripCodeFence(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            s = firstNewLineSafe(s, firstNewline);
            int lastFence = s.lastIndexOf("```");
            if (lastFence > 0) {
                s = s.substring(0, lastFence);
            }
        }
        return s.trim();
    }

    private String firstNewLineSafe(String s, int idx) {
        return idx >= 0 ? s.substring(idx + 1) : s;
    }
}
