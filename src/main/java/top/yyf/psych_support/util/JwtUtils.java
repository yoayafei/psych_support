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
        // 使用固定密钥（必须 ≥32 字符）
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.EXPIRATION_TIME = expirationMs; // 默认 2 小时（7200000 ms）
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
            if (isTokenBlacklisted(token)) {
                throw new RuntimeException("Token已失效，请重新登录");
            }
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("Token验证失败: " + e.getMessage());
        }
    }

    public void addToBlacklist(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date expiration = claims.getExpiration();
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + token, 
                    "logout", 
                    ttl, 
                    TimeUnit.MILLISECONDS
                );
                log.info("Token已加入黑名单，剩余有效期: {} 毫秒", ttl);
            }
            redisTemplate.delete(TOKEN_USER_PREFIX + token);
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