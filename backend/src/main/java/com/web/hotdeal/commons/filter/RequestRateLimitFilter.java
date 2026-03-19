package com.web.hotdeal.commons.filter;

import com.web.hotdeal.commons.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestRateLimitFilter extends OncePerRequestFilter {

    private static final String API_PATH_PATTERN = "/api/**";
    private static final String ADMIN_PATH_PATTERN = "/api/admin/**";
    private static final String PUBLIC_SCOPE = "public";
    private static final String ADMIN_SCOPE = "admin";

    private static final String RATE_LIMIT_LUA = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refillTokens = tonumber(ARGV[2])
            local refillPeriodMillis = tonumber(ARGV[3])
            local requestedTokens = tonumber(ARGV[4])
            local nowMillis = tonumber(ARGV[5])
            local ttlSeconds = tonumber(ARGV[6])

            local state = redis.call('HMGET', key, 'tokens', 'timestamp')
            local tokens = tonumber(state[1])
            local timestamp = tonumber(state[2])

            if tokens == nil or timestamp == nil then
              tokens = capacity
              timestamp = nowMillis
            end

            if nowMillis > timestamp then
              local elapsed = nowMillis - timestamp
              local periods = math.floor(elapsed / refillPeriodMillis)
              if periods > 0 then
                local refillAmount = periods * refillTokens
                tokens = math.min(capacity, tokens + refillAmount)
                timestamp = timestamp + (periods * refillPeriodMillis)
              end
            end

            local allowed = 0
            local retryAfterMillis = 0

            if tokens >= requestedTokens then
              tokens = tokens - requestedTokens
              allowed = 1
            else
              local missing = requestedTokens - tokens
              local periodsNeeded = math.ceil(missing / refillTokens)
              retryAfterMillis = periodsNeeded * refillPeriodMillis - (nowMillis - timestamp)
              if retryAfterMillis < 0 then
                retryAfterMillis = refillPeriodMillis
              end
            end

            redis.call('HMSET', key, 'tokens', tostring(tokens), 'timestamp', tostring(timestamp))
            redis.call('EXPIRE', key, ttlSeconds)

            return {allowed, tokens, retryAfterMillis}
            """;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitProperties rateLimitProperties;
    private final RedisScript<List> redisScript = createRateLimitScript();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        return !antPathMatcher.match(API_PATH_PATTERN, request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        RateLimitProperties.Rule rule = resolveRule(requestUri);
        String clientKey = resolveClientKey(request);
        String redisKey = buildRedisKey(requestUri, clientKey);

        RateLimitDecision decision;
        try {
            decision = consumeToken(rule, redisKey);
            if (decision == null) {
                handleLimiterFailure(response, filterChain, request, "rate-limit script returned no decision");
                return;
            }
        } catch (Exception e) {
            handleLimiterFailure(response, filterChain, request, e.getMessage());
            return;
        }

        response.setHeader("X-RateLimit-Limit", Long.toString(rule.getCapacity()));
        response.setHeader("X-RateLimit-Remaining", Long.toString(decision.remainingTokens()));

        if (!decision.allowed()) {
            long retryAfterSeconds = Math.max(1L, (decision.retryAfterMillis() + 999) / 1000);
            response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Too many requests\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitDecision consumeToken(RateLimitProperties.Rule rule, String redisKey) {
        long refillMillis = Math.max(1L, rule.getRefillSeconds() * 1000);
        long periodsToFill = Math.max(1L, (rule.getCapacity() + rule.getRefillTokens() - 1) / rule.getRefillTokens());
        long ttlSeconds = Math.max(1L, periodsToFill * rule.getRefillSeconds() * 2);

        List<?> rawResult = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                Long.toString(rule.getCapacity()),
                Long.toString(rule.getRefillTokens()),
                Long.toString(refillMillis),
                "1",
                Long.toString(System.currentTimeMillis()),
                Long.toString(ttlSeconds)
        );
        return toDecision(rawResult);
    }

    private RateLimitProperties.Rule resolveRule(String requestUri) {
        if (antPathMatcher.match(ADMIN_PATH_PATTERN, requestUri)) {
            return rateLimitProperties.getAdminApi();
        }
        return rateLimitProperties.getPublicApi();
    }

    private String buildRedisKey(String requestUri, String clientKey) {
        String scope = antPathMatcher.match(ADMIN_PATH_PATTERN, requestUri) ? ADMIN_SCOPE : PUBLIC_SCOPE;
        return "%s:%s:%s".formatted(rateLimitProperties.getRedisKeyPrefix(), scope, clientKey);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void handleLimiterFailure(
            HttpServletResponse response,
            FilterChain filterChain,
            HttpServletRequest request,
            String reason
    ) throws IOException, ServletException {
        if (rateLimitProperties.isFailOpen()) {
            log.warn("Rate limiter unavailable for uri={}, reason={}. Request allowed due to fail-open.",
                    request.getRequestURI(), reason);
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"Rate limiter unavailable\"}");
    }

    private RateLimitDecision toDecision(List<?> rawResult) {
        if (rawResult == null || rawResult.size() < 3) {
            return null;
        }

        Long allowed = toLong(rawResult.get(0));
        Long remaining = toLong(rawResult.get(1));
        Long retryAfterMillis = toLong(rawResult.get(2));
        if (allowed == null || remaining == null || retryAfterMillis == null) {
            return null;
        }

        return new RateLimitDecision(allowed == 1L, Math.max(remaining, 0L), Math.max(retryAfterMillis, 0L));
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private RedisScript<List> createRateLimitScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(RATE_LIMIT_LUA);
        script.setResultType(List.class);
        return script;
    }

    private record RateLimitDecision(boolean allowed, long remainingTokens, long retryAfterMillis) {
    }
}
