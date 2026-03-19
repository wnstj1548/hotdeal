package com.web.hotdeal.commons.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private boolean failOpen = true;
    private String redisKeyPrefix = "rate-limit";

    @Valid
    private Rule publicApi = new Rule(120, 120, 60);

    @Valid
    private Rule adminApi = new Rule(20, 20, 60);

    @Getter
    @Setter
    public static class Rule {
        @Min(1)
        private long capacity;

        @Min(1)
        private long refillTokens;

        @Min(1)
        private long refillSeconds;

        public Rule() {
            this(120, 120, 60);
        }

        public Rule(long capacity, long refillTokens, long refillSeconds) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillSeconds = refillSeconds;
        }
    }
}
