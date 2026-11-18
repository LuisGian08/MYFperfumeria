package com.myf.auth.service;

import com.myf.auth.entity.Rol;
import com.myf.auth.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private static final String SECRET = "myf-unit-test-secret-key-at-least-32-bytes";

    @Test
    void tokenIncludesUserRolesForGatewayAuthorization() {
        Usuario user = new Usuario();
        user.setId(42L);
        user.setEmail("admin@example.com");
        Rol admin = new Rol();
        admin.setNombre("ADMIN");
        user.getRoles().add(admin);

        String token = new JwtService(SECRET, 60).create(user);
        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.get("roles")).isEqualTo(List.of("ADMIN"));
    }
}
