// top/yyf/psych_support/config/JwtAuthInterceptor.java
package top.yyf.psych_support.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.yyf.psych_support.util.JwtUtils;

import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 公开接口放行
        if (uri.equals("/api/assessments") ||
                uri.matches("/api/assessments/\\d+$") ||
                uri.startsWith("/api/users/login") ||
                uri.startsWith("/api/users/register")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "缺少Token");
            return false;
        }

        // ✅ 提取 token（兼容 Bearer 前缀）
        String token = authHeader;
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7); // "Bearer ".length() = 7，注意有空格
        }
        token = token.trim();

        if (token.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token格式错误");
            return false;
        }

        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            request.setAttribute("currentUserId", userId);
            return true;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token无效或已过期");
            return false;
        }
    }
}