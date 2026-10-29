package com.jzbook.accountbook.dto;

/** 登录 / 注册成功的返回体 */
public record AuthResponse(Long userId, String nickname, String token) {
}
