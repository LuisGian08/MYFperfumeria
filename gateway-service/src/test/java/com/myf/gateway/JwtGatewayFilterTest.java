package com.myf.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtGatewayFilterTest {
    private static final String SECRET = "myf-unit-test-secret-key-at-least-32-bytes";

    @Test
    void validJwtIsForwardedAsUserHeadersToProtectedRoute() {
        JwtGatewayFilter filter = new JwtGatewayFilter(SECRET);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject("user@example.com").claim("userId", 42L).signWith(key).compact();
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/productos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, next -> {
            forwarded.set(next);
            return reactor.core.publisher.Mono.empty();
        }).block();

        assertThat(forwarded.get()).isNotNull();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-User-Email"))
                .isEqualTo("user@example.com");
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("42");
    }
}
