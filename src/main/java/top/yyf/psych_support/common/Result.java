package top.yyf.psych_support.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果
 *
 * @param <T> 数据类型
 * @author mqxu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;


    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应(带数据)
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(
                ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMessage(),
                data
        );
    }

    /**
     * 成功响应(自定义消息)
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(
                ResultCode.SUCCESS.getCode(),
                message,
                data
        );
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return error(ResultCode.ERROR);
    }

    /**
     * 失败响应(自定义消息)
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(
                ResultCode.ERROR.getCode(),
                message,
                null
        );
    }

    /**
     * 失败响应(ResultCode)
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(
                resultCode.getCode(),
                resultCode.getMessage(),
                null
        );
    }

    /**
     * 失败响应(自定义码和消息)
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(
                code,
                message,
                null
        );
    }

    /**
     * 根据条件返回成功或失败
     */
    public static <T> Result<T> status(boolean flag) {
        return flag ? success() : error();
    }

}