package com.jzbook.accountbook;

import com.jzbook.accountbook.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口层测试（MockMvc）：不启动真实端口，直接模拟 HTTP 请求
 * 重点覆盖：鉴权拦截、Token 伪造、参数校验 —— 这三类是最容易在重构中被改坏的
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    /** 合法 Token（签名有效、未过期），用于需要登录的用例 */
    private String validToken() {
        return "Bearer " + tokenService.create(2L);
    }

    @Test
    void listWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/record/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void listWithForgedTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/record/list").header("Authorization", "Bearer abc.def"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void aiEndpointWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(post("/ai/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"午饭38\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void listWithValidTokenShouldReturnCode0() throws Exception {
        mockMvc.perform(get("/record/list").header("Authorization", validToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void addRecordWithZeroAmountShouldReturn400() throws Exception {
        String body = "{\"type\":\"1\",\"category\":\"餐饮\",\"amount\":\"0\",\"recordDate\":\"2026-10-29\"}";
        mockMvc.perform(post("/record/add")
                        .header("Authorization", validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void addRecordWithInvalidTypeShouldReturn400() throws Exception {
        String body = "{\"type\":\"3\",\"category\":\"餐饮\",\"amount\":\"20\"}";
        mockMvc.perform(post("/record/add")
                        .header("Authorization", validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void addRecordWithBlankCategoryShouldReturn400() throws Exception {
        String body = "{\"type\":\"1\",\"category\":\"\",\"amount\":\"20\"}";
        mockMvc.perform(post("/record/add")
                        .header("Authorization", validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void registerWithShortUsernameShouldReturn400() throws Exception {
        String body = "{\"username\":\"ab\",\"password\":\"123456\"}";
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void registerWithShortPasswordShouldReturn400() throws Exception {
        String body = "{\"username\":\"normaluser001\",\"password\":\"123\"}";
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void addRecordSuccessShouldReturnId() throws Exception {
        String body = "{\"type\":\"1\",\"category\":\"餐饮\",\"amount\":\"12.34\","
                + "\"recordDate\":\"2026-10-29\",\"remark\":\"api测试\"}";
        mockMvc.perform(post("/record/add")
                        .header("Authorization", validToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber());
    }
}
