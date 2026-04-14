package top.yyf.psych_support.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtils {

    // ✅ 改为从配置读取密钥（需在 application.yml 中设置）
    private final SecretKey key;
    
    // ✅ 改为从配置读取过期时间（单位：毫秒）
    private final long EXPIRATION_TIME;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String TOKEN_USER_PREFIX = "token:user:";

    // 构造函数注入配置（通过 @Value）
    public JwtUtils(
            @org.springframework.beans.factory.annotation.Value("${jwt.secret}") String secret,
            @org.springframework.beans.factory.annotation.Value("${jwt.expiration-ms:7200000}") long expirationMs
    ) {
        // ✅ 确保密钥至少32字节
        byte[] keyBytes;
        if (secret.getBytes().length < 32) {
            // 如果密钥太短，填充到32字节
            keyBytes = new byte[32];
            byte[] original = secret.getBytes();
            System.arraycopy(original, 0, keyBytes, 0, Math.min(original.length, 32));
        } else {
            keyBytes = secret.getBytes();
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.EXPIRATION_TIME = expirationMs;
        log.info("JWT初始化完成，密钥长度: {} 字节", keyBytes.length);
    }

    public String generateToken(Long userId) {
        String token = Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 保留你原有的 Redis 关联（用于管理）
        redisTemplate.opsForValue().set(
            TOKEN_USER_PREFIX + token, 
            userId.toString(), 
            EXPIRATION_TIME, 
            TimeUnit.MILLISECONDS
        );
        return token;
    }

    public Long getUserIdFromToken(String token) {
        try {
            // ✅ 先清理空白字符
            String cleanToken = token.trim().replaceAll("\\s", "");

            // ✅ 移除 Bearer 前缀（如果存在）
            if (cleanToken.startsWith("Bearer")) {
                cleanToken = cleanToken.substring(6); // "Bearer".length() = 6
                log.info("移除了 Bearer 前缀");
            }

            // ✅ 再次清理可能的新空白字符
            cleanToken = cleanToken.trim();

            log.info("验证Token - 清理后Token前20字符: {}",
                    cleanToken.substring(0, Math.min(20, cleanToken.length())));

            if (isTokenBlacklisted(cleanToken)) {
                throw new RuntimeException("Token已失效，请重新登录");
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();

            Long userId = Long.parseLong(claims.getSubject());
            log.info("Token验证成功 - 用户ID: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage(), e);
            throw new RuntimeException("Token验证失败: " + e.getMessage());
        }
    }

    public void addToBlacklist(String token) {
        try {
            // 同样需要清理 token
            String cleanToken = token.trim().replaceAll("\\s", "");
            if (cleanToken.startsWith("Bearer")) {
                cleanToken = cleanToken.substring(6);
                cleanToken = cleanToken.trim();
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();
            Date expiration = claims.getExpiration();
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + cleanToken,
                        "logout",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
                log.info("Token已加入黑名单，剩余有效期: {} 毫秒", ttl);
            }
            redisTemplate.delete(TOKEN_USER_PREFIX + cleanToken);
        } catch (Exception e) {
            log.warn("将token加入黑名单失败: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
        } catch (Exception e) {
            return 0;
        }
    }
}