package uz.drivesmart.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uz.drivesmart.exception.BusinessException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting interceptor
 * Prevents abuse and bot attacks
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_TEST_STARTS_PER_HOUR = 10;

    private final ConcurrentHashMap<String, RateLimitData> requestLimits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateLimitData> testStartLimits = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String ipAddress = getClientIP(request);
        String endpoint = request.getRequestURI();

        // General rate limiting
        if (!checkRateLimit(ipAddress, requestLimits, MAX_REQUESTS_PER_MINUTE, 60000)) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            throw new BusinessException("Haddan tashqari so'rovlar. Bir necha daqiqadan so'ng urinib ko'ring");
        }

        // Test start specific rate limiting
        if (endpoint.contains("/tests/start")) {
            if (!checkRateLimit(ipAddress + "_test", testStartLimits, MAX_TEST_STARTS_PER_HOUR, 3600000)) {
                log.warn("Test start rate limit exceeded for IP: {}", ipAddress);
                throw new BusinessException("Soatiga maksimal " + MAX_TEST_STARTS_PER_HOUR + " ta test boshlash mumkin");
            }
        }

        return true;
    }

    private boolean checkRateLimit(String key, ConcurrentHashMap<String, RateLimitData> map,
                                   int maxRequests, long windowMs) {
        RateLimitData data = map.computeIfAbsent(key, k -> new RateLimitData());

        long now = System.currentTimeMillis();

        // Reset if window expired
        if (now - data.windowStart > windowMs) {
            data.counter.set(0);
            data.windowStart = now;
        }

        int count = data.counter.incrementAndGet();
        return count <= maxRequests;
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static class RateLimitData {
        AtomicInteger counter = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
    }

    // Clean up old entries every hour
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 3600000)
    public void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        requestLimits.entrySet().removeIf(entry ->
                now - entry.getValue().windowStart > 3600000);
        testStartLimits.entrySet().removeIf(entry ->
                now - entry.getValue().windowStart > 7200000);
        log.info("Rate limit cache cleaned. Size: requests={}, testStarts={}",
                requestLimits.size(), testStartLimits.size());
    }
}