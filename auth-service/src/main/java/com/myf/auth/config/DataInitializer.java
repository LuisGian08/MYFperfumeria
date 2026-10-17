package com.myf.auth.config;
import com.myf.auth.entity.Usuario; import com.myf.auth.repository.UsuarioRepository; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class DataInitializer {
 @Bean CommandLineRunner seedAdmin(UsuarioRepository repo, PasswordEncoder encoder){ return args->{ if(!repo.existsByEmailIgnoreCase("admin@myf.local")){ Usuario u=new Usuario();u.setNombres("Administrador");u.setApellidos("MYF");u.setEmail("admin@myf.local");u.setPasswordHash(encoder.encode("Admin123*"));repo.save(u); } }; }
}
