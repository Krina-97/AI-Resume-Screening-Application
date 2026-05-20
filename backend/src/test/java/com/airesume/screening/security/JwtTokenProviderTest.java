package com.airesume.screening.security;

import com.airesume.screening.config.AppProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class JwtTokenProviderTest {

    @Test
    void generatesAndValidatesToken() {
        AppProperties props = new AppProperties();
        props.getJwt().setSecret("unit-test-secret-key-must-be-long-enough!!");
        props.getJwt().setExpirationMs(60_000L);

        JwtTokenProvider provider = new JwtTokenProvider(props);
        String token = provider.generateToken("hruser", Map.of("role", "HR"));

        Assertions.assertTrue(provider.validateToken(token, "hruser"));
        Assertions.assertEquals("hruser", provider.getUsername(token));
    }
}
