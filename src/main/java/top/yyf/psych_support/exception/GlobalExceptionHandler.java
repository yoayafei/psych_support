package top.yyf.psych_support.exception;




import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.yyf.psych_support.common.Result;
import top.yyf.psych_support.common.ResultCode;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("服务器内部错误");
    }


    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage()); // 返回具体错误信息
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public Result handleServletRequestBindingException(ServletRequestBindingException e) {
        log.error("请求参数绑定异常: {}", e.getMessage());
        return Result.error("缺少必要的请求属性: " + e.getMessage());
    }
}