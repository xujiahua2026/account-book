package com.jzbook.accountbook.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器：校验 Authorization: Bearer <token>
 * 通过后把 userId 放入 request 属性，Controller 用 @RequestAttribute 取，
 * 从而彻底摆脱「userId 由前端传、可伪造」的问题。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "userId";

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 浏览器跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7).trim();
        }

        Long userId = tokenService.verify(token);
        if (userId == null) {
            log.debug("未通过鉴权: {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(response);
            return false;
        }

        request.setAttribute(ATTR_USER_ID, userId);
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期，请重新登录\"}");
    }
}
