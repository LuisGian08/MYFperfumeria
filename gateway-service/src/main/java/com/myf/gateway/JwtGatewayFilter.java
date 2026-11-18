package com.myf.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private final SecretKey key;
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login", "/actuator/health", "/actuator/prometheus", "/swagger-ui", "/v3/api-docs");
    public JwtGatewayFilter(@Value("${jwt.secret}") String secret){this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}
    @Override public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain){
        String path=exchange.getRequest().getURI().getPath();HttpMethod method=exchange.getRequest().getMethod();
        if(PUBLIC_PATHS.stream().anyMatch(path::startsWith)) return chain.filter(exchange);
        boolean publicProduct = path.equals("/api/productos") || path.matches("/api/productos/\\d+");
        if(HttpMethod.GET.equals(method)&&publicProduct) return chain.filter(exchange);
        if(HttpMethod.POST.equals(method)&&path.equals("/api/ventas/checkout")) return chain.filter(exchange);
        String header=exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(header==null||!header.startsWith("Bearer ")){exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);return exchange.getResponse().setComplete();}
        try{
            Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
            Object roles=claims.get("roles");
            boolean isAdmin=roles instanceof Collection<?> values&&values.contains("ADMIN");
            if(!isAdmin){exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);return exchange.getResponse().setComplete();}
            ServerWebExchange mutated=exchange.mutate().request(b->b.header("X-User-Email",claims.getSubject()).header("X-User-Id",String.valueOf(claims.get("userId"))).header("X-User-Role","ADMIN")).build();
            return chain.filter(mutated);
        }catch(Exception ex){exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);return exchange.getResponse().setComplete();}
    }
    @Override public int getOrder(){return -100;}
}
