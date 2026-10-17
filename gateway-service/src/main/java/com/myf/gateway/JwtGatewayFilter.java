package com.myf.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final SecretKey key;

    private static final List<String> PUBLIC_PATHS =
            List.of(
                    "/api/auth/login",
                    "/actuator/health"
            );

    public JwtGatewayFilter(
            @Value("${jwt.secret}") String secret
    ) {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        String path =
                exchange.getRequest()
                        .getURI()
                        .getPath();

        HttpMethod method =
                exchange.getRequest()
                        .getMethod();


        // ======================================================
        // PERMITIR PETICIONES OPTIONS DEL NAVEGADOR
        // ======================================================

        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }


        // ======================================================
        // RUTAS PÚBLICAS GENERALES
        // ======================================================

        if (
                PUBLIC_PATHS
                        .stream()
                        .anyMatch(path::startsWith)
        ) {
            return chain.filter(exchange);
        }


        // ======================================================
        // CATÁLOGO E INVENTARIO PÚBLICO
        // ======================================================

        boolean publicInventory =
                path.equals("/api/inventario") ||
                        path.matches("/api/inventario/\\d+");

        if (
                HttpMethod.GET.equals(method) &&
                        (
                                path.startsWith("/api/productos") ||
                                        publicInventory ||
                                        path.startsWith("/api/marcas") ||
                                        path.startsWith("/api/categorias")
                        )
        ) {
            return chain.filter(exchange);
        }


        // ======================================================
        // CHECKOUT PÚBLICO
        // ======================================================

        if (
                HttpMethod.POST.equals(method) &&
                        path.equals("/api/ventas/checkout")
        ) {
            return chain.filter(exchange);
        }


        // ======================================================
        // SUBIR COMPROBANTE - PÚBLICO
        //
        // Ejemplos:
        // /api/ventas/18/comprobante
        // /api/ventas/19/comprobante
        // ======================================================

        boolean publicComprobante =
                HttpMethod.POST.equals(method) &&
                        path.startsWith("/api/ventas/") &&
                        path.endsWith("/comprobante");

        if (publicComprobante) {
            return chain.filter(exchange);
        }


        // ======================================================
        // DESDE AQUÍ SE REQUIERE TOKEN
        // ======================================================

        String header =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                HttpHeaders.AUTHORIZATION
                        );

        if (
                header == null ||
                        !header.startsWith("Bearer ")
        ) {

            exchange.getResponse()
                    .setStatusCode(
                            HttpStatus.UNAUTHORIZED
                    );

            return exchange.getResponse()
                    .setComplete();
        }


        // ======================================================
        // VALIDAR TOKEN JWT
        // ======================================================

        try {

            Claims claims =
                    Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(
                                    header.substring(7)
                            )
                            .getPayload();


            ServerWebExchange mutated =
                    exchange.mutate()
                            .request(
                                    builder ->
                                            builder
                                                    .header(
                                                            "X-User-Email",
                                                            claims.getSubject()
                                                    )
                                                    .header(
                                                            "X-User-Id",
                                                            String.valueOf(
                                                                    claims.get("userId")
                                                            )
                                                    )
                            )
                            .build();


            return chain.filter(mutated);

        } catch (Exception ex) {

            exchange.getResponse()
                    .setStatusCode(
                            HttpStatus.UNAUTHORIZED
                    );

            return exchange.getResponse()
                    .setComplete();
        }
    }


    @Override
    public int getOrder() {
        return -100;
    }
}