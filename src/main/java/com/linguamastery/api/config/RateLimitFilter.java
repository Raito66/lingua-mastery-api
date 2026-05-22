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
    // 忘記密碼 / 重寄驗證信：每 IP 每 10 分鐘最多 3 次
    private final Map<String, Bucket> emailBuckets = new ConcurrentHashMap<>();

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
        } else if (path.equals("/api/auth/forgot-password") || path.equals("/api/auth/resend-verification")) {
            bucket = emailBuckets.computeIfAbsent(getClientIp(request), ip ->
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

    /**
     * 取得客戶端真實 IP。
     * 在 Render 反向代理環境下，使用 X-Forwarded-For 的最後一個 IP（由 Render LB 附加，不可被客戶端偽造）。
     * 若 header 不存在則 fallback 至 RemoteAddr。
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] ips = forwarded.split(",");
            // 最後一個 IP 由最近的受信任代理附加，防止客戶端偽造 header 繞過限流
            return ips[ips.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}
