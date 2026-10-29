package com.jzbook.accountbook.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一所有接口的异常返回格式：{ "code": xxx, "msg": "xxx" }
 * 好处：Controller 不用再写 try-catch，异常信息不暴露堆栈给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务校验失败：参数为空、用户名重复等 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException e) {
        return build(400, e.getMessage());
    }

    /** 认证失败：登录凭证错误 */
    @ExceptionHandler(AuthException.class)
    public Map<String, Object> handleAuth(AuthException e) {
        return build(401, e.getMessage());
    }

    /** 请求体不是合法 JSON */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Map<String, Object> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return build(400, "请求格式有误，请提交合法的 JSON");
    }

    /** 参数类型不匹配，如 userId 传了 "abc" */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return build(400, "参数 " + e.getName() + " 格式错误");
    }

    /** 缺少必填的查询参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Map<String, Object> handleMissingParam(MissingServletRequestParameterException e) {
        return build(400, "缺少必要参数：" + e.getParameterName());
    }

    /** 外部依赖异常（如大模型不可用），把提示返回给用户便于排查，但不暴露堆栈 */
    @ExceptionHandler(IllegalStateException.class)
    public Map<String, Object> handleIllegalState(IllegalStateException e) {
        log.error("服务状态异常: {}", e.getMessage());
        return build(500, e.getMessage() == null ? "服务暂时不可用" : e.getMessage());
    }

    /** 其余未预料的异常：记录日志，但不把内部细节暴露给前端 */
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleOther(Exception e) {
        log.error("系统异常", e);
        return build(500, "系统繁忙，请稍后重试");
    }

    private Map<String, Object> build(int code, String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }
}
