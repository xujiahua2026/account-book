package com.jzbook.accountbook.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 轻量 Token 服务：HMAC-SHA256 签名，格式为 base64(userId:过期秒) + "." + 签名
 *
 * 说明：这里不引第三方 JWT 库，用 JDK 自带的 Mac 实现同等效果（防篡改 + 过期校验），
 * 目的是把「无状态凭证」的原理讲清楚；生产环境可直接替换为 jjwt / Spring Security。
 */
@Component
public class TokenService {

    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();
    private static final String ALGORITHM = "HmacSHA256";

    /** 签名密钥，生产环境应通过环境变量注入，不要提交到代码仓库 */
    @Value("${app.token.secret:jzbook-default-secret-please-change}")
    private String secret;

    /** Token 有效期（小时） */
    @Value("${app.token.expire-hours:72}")
    private long expireHours;

    /** 签发 Token */
    public String create(Long userId) {
        long expireAt = System.currentTimeMillis() / 1000 + expireHours * 3600;
        String payload = userId + ":" + expireAt;
        String encoded = ENC.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + sign(encoded);
    }

    /**
     * 校验 Token，合法返回 userId，非法或过期返回 null
     * 校验顺序：签名是否被篡改 → 格式是否正确 → 是否过期
     */
    public Long verify(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        int idx = token.lastIndexOf('.');
        if (idx <= 0) {
            return null;
        }
        String encoded = token.substring(0, idx);
        String signature = token.substring(idx + 1);

        // 用定长时间比较，避免时序攻击
        if (!MessageDigest.isEqual(
                sign(encoded).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }

        try {
            String payload = new String(DEC.decode(encoded), StandardCharsets.UTF_8);
            String[] parts = payload.split(":");
            if (parts.length != 2) {
                return null;
            }
            long expireAt = Long.parseLong(parts[1]);
            if (System.currentTimeMillis() / 1000 > expireAt) {
                return null;
            }
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return ENC.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Token 签名失败", e);
        }
    }
}
