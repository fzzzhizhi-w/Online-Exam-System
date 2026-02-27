package com.jepai.exam.common.response;

import lombok.Data;

/**
 * 统一响应结果封装
 */
@Data
public class Result<T> {
    /** 状态码 */
    private Integer code;
    /** 提示信息 */
    private String message;
    /** 数据 */
    private T data;

    private Result() {}

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> unauthorized() {
        return fail(401, "未登录或登录已过期，请重新登录");
    }

    public static <T> Result<T> forbidden() {
        return fail(403, "无权限访问");
    }
}
