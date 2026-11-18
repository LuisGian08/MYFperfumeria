package com.myf.auth.config;
import com.myf.auth.entity.Rol;
import com.myf.auth.entity.Usuario;
import com.myf.auth.repository.RolRepository;
import com.myf.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedAdmin(UsuarioRepository users, RolRepository roles, PasswordEncoder encoder) {
        return args -> {
            Rol adminRole = roles.findByNombreIgnoreCase("ADMIN").orElseGet(() -> {
                Rol role = new Rol();
                role.setNombre("ADMIN");
                role.setDescripcion("Administrador del sistema");
                return roles.save(role);
            });

            Usuario admin = users.findByEmailIgnoreCase("admin@myf.local").orElseGet(() -> {
                Usuario user = new Usuario();
                user.setNombres("Administrador");
                user.setApellidos("MYF");
                user.setEmail("admin@myf.local");
                user.setPasswordHash(encoder.encode("Admin123*"));
                return user;
            });
            admin.getRoles().add(adminRole);
            users.save(admin);
        };
    }
}
