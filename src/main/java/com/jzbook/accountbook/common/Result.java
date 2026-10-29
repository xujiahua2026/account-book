package com.jzbook.accountbook.common;

/**
 * 统一响应结构
 * { "code": 0, "msg": "成功", "data": {...} }
 *
 * code 约定：0 成功、400 参数错误、401 未认证、404 资源不存在、500 系统异常
 */
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "成功", data);
    }

    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(0, msg, data);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
