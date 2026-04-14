package top.yyf.psych_support.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yyf.psych_support.util.JwtUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/token-test")
    public Map<String, Object> testToken(@RequestHeader(value="Authorization", required=false) String authHeader) {
        Map<String, Object> result = new HashMap<>();

        // 生成新token
        Long testUserId = 7L;
        String newToken = jwtUtils.generateToken(testUserId);
        result.put("generatedToken", newToken);

        // 立即解析
        try {
            Long userId = jwtUtils.getUserIdFromToken(newToken);
            result.put("parsedUserId", userId);
            result.put("parseSuccess", true);
        } catch (Exception e) {
            result.put("parseError", e.getMessage());
            result.put("parseSuccess", false);
        }

        // 如果提供了Authorization头，也解析它
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = jwtUtils.getUserIdFromToken(token);
                result.put("headerUserId", userId);
            } catch (Exception e) {
                result.put("headerParseError", e.getMessage());
            }
        }

        return result;
    }
}