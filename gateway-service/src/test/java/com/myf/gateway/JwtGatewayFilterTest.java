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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtGatewayFilterTest {
    private static final String SECRET = "myf-unit-test-secret-key-at-least-32-bytes";

    @Test
    void validJwtIsForwardedAsUserHeadersToProtectedRoute() {
        JwtGatewayFilter filter = new JwtGatewayFilter(SECRET);
        String token = tokenWithRoles(List.of("ADMIN"));
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

    @Test
    void inventoryGetRequiresAdminRole() {
        JwtGatewayFilter filter = new JwtGatewayFilter(SECRET);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/inventario/1002").build());

        filter.filter(exchange, next -> {
            throw new AssertionError("Una solicitud sin token no debe llegar al servicio");
        }).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validNonAdminTokenIsForbiddenForInventory() {
        JwtGatewayFilter filter = new JwtGatewayFilter(SECRET);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/inventario/1002")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithRoles(List.of("VENDEDOR")))
                        .build());

        filter.filter(exchange, next -> {
            throw new AssertionError("Un usuario sin rol ADMIN no debe llegar al servicio");
        }).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void publicCatalogAndCheckoutDoNotRequireToken() {
        JwtGatewayFilter filter = new JwtGatewayFilter(SECRET);
        ServerWebExchange catalog = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/productos/1002").build());
        ServerWebExchange checkout = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/ventas/checkout").build());
        AtomicReference<ServerWebExchange> forwardedCatalog = new AtomicReference<>();
        AtomicReference<ServerWebExchange> forwardedCheckout = new AtomicReference<>();

        filter.filter(catalog, next -> {
            forwardedCatalog.set(next);
            return reactor.core.publisher.Mono.empty();
        }).block();
        filter.filter(checkout, next -> {
            forwardedCheckout.set(next);
            return reactor.core.publisher.Mono.empty();
        }).block();

        assertThat(forwardedCatalog.get()).isNotNull();
        assertThat(forwardedCheckout.get()).isNotNull();
    }

    private String tokenWithRoles(List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().subject("user@example.com").claim("userId", 42L)
                .claim("roles", roles).signWith(key).compact();
    }
}
