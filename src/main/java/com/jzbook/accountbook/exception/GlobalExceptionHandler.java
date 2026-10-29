package com.jzbook.accountbook.exception;

import com.jzbook.accountbook.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一所有接口的异常返回格式：{ "code": xxx, "msg": "xxx", "data": null }
 * 好处：Controller 不用再写 try-catch，异常堆栈不外泄
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 参数校验失败：@Valid 触发，返回全部字段错误（；分隔） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.fail(400, msg.isEmpty() ? "参数不合法" : msg);
    }

    /** 业务校验失败：用户名重复、金额异常等 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.fail(400, e.getMessage());
    }

    /** 认证失败：登录凭证错误 */
    @ExceptionHandler(AuthException.class)
    public Result<Void> handleAuth(AuthException e) {
        return Result.fail(401, e.getMessage());
    }

    /** 请求体不是合法 JSON（含字段类型不匹配，如 type 传了字母） */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.fail(400, "请求格式有误，请提交合法的 JSON（检查字段类型与日期格式 yyyy-MM-dd）");
    }

    /** 参数类型不匹配，如 path/query 参数传了非法值 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail(400, "参数 " + e.getName() + " 格式错误");
    }

    /** 缺少必填的查询参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少必要参数：" + e.getParameterName());
    }

    /** 外部依赖异常（如大模型不可用）：提示返回给用户便于排查，但不暴露堆栈 */
    @ExceptionHandler(IllegalStateException.class)
    public Result<Void> handleIllegalState(IllegalStateException e) {
        log.error("服务状态异常: {}", e.getMessage());
        return Result.fail(500, e.getMessage() == null ? "服务暂时不可用" : e.getMessage());
    }

    /** 其余未预料的异常：记录日志，但不把内部细节暴露给前端 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
