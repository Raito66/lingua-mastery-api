package com.linguamastery.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    // 登入：每 IP 每分鐘最多 5 次
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    // 註冊：每 IP 每 10 分鐘最多 3 次
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();

        Bucket bucket = null;
        if (path.equals("/api/auth/login")) {
            bucket = loginBuckets.computeIfAbsent(getClientIp(request), ip ->
                    Bucket.builder()
                            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                            .build());
        } else if (path.equals("/api/auth/register")) {
            bucket = registerBuckets.computeIfAbsent(getClientIp(request), ip ->
                    Bucket.builder()
                            .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(10))))
                            .build());
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"請求過於頻繁，請稍後再試\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        // 取 X-Forwarded-For（Render 反向代理會加這個 header）
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
