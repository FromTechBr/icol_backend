package com.br.org.icol.icolbackend.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

@Component
public class LoginRateLimiter {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    public boolean tryAcquire(String email, String remoteAddress) {
        return consume("email:" + email.trim().toLowerCase(Locale.ROOT))
                && consume("ip:" + remoteAddress);
    }

    private boolean consume(String key) {
        Bucket bucket = buckets.get(key, ignored -> newBucket());
        return bucket.tryConsume(1);
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15))))
                .build();
    }
}
