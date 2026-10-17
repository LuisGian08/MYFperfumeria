package com.myf.auth.service;
import com.myf.auth.dto.*; import com.myf.auth.entity.Usuario; import com.myf.auth.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Service
public class AuthService {
 private final UsuarioRepository repo; private final PasswordEncoder encoder; private final JwtService jwt;
 public AuthService(UsuarioRepository repo,PasswordEncoder encoder,JwtService jwt){this.repo=repo;this.encoder=encoder;this.jwt=jwt;}
 @Transactional public AuthResponse login(LoginRequest r){ Usuario u=repo.findByEmailIgnoreCase(r.email()).orElseThrow(()->new IllegalArgumentException("Credenciales inválidas")); if(!"ACTIVO".equals(u.getEstado())||!encoder.matches(r.password(),u.getPasswordHash())) throw new IllegalArgumentException("Credenciales inválidas"); u.setUltimoAcceso(LocalDateTime.now()); repo.save(u); return new AuthResponse(jwt.create(u),u.getId(),u.getEmail(),u.getNombres()); }
 @Transactional public AuthResponse register(RegisterRequest r){ if(repo.existsByEmailIgnoreCase(r.email())) throw new IllegalArgumentException("El correo ya existe"); Usuario u=new Usuario(); u.setNombres(r.nombres());u.setApellidos(r.apellidos());u.setEmail(r.email().toLowerCase());u.setPasswordHash(encoder.encode(r.password()));u=repo.save(u); return new AuthResponse(jwt.create(u),u.getId(),u.getEmail(),u.getNombres()); }
}
