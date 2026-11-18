package com.myf.auth.service;

import com.myf.auth.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
 private final SecretKey key; private final long minutes;
 public JwtService(@Value("${jwt.secret}") String secret,@Value("${jwt.expiration-minutes:480}") long minutes){ this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.minutes=minutes; }
 public String create(Usuario u){ Instant now=Instant.now(); List<String> roles=u.getRoles().stream().map(role->role.getNombre()).sorted().toList(); return Jwts.builder().subject(u.getEmail()).claim("userId",u.getId()).claim("name",u.getNombres()).claim("roles",roles).issuedAt(Date.from(now)).expiration(Date.from(now.plus(minutes, ChronoUnit.MINUTES))).signWith(key).compact(); }
}
