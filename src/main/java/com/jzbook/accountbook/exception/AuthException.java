package com.jzbook.accountbook.exception;

/**
 * 认证失败异常（登录凭证错误），由全局处理器转成 401
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
