package com.br.org.icol.icolbackend;

import com.br.org.icol.icolbackend.service.LoginRateLimiter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTests {

    @Test
    void limitaTentativasPorEmailEIp() {
        LoginRateLimiter limiter = new LoginRateLimiter();

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThat(limiter.tryAcquire("user@example.org", "192.0.2.10")).isTrue();
        }

        assertThat(limiter.tryAcquire("user@example.org", "192.0.2.10")).isFalse();
        assertThat(limiter.tryAcquire("other@example.org", "192.0.2.10")).isFalse();
        assertThat(limiter.tryAcquire("user@example.org", "192.0.2.11")).isFalse();
    }
}
