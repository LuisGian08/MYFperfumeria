package com.myf.auth.service;

import com.myf.auth.dto.AuthResponse;
import com.myf.auth.dto.RegisterRequest;
import com.myf.auth.entity.Usuario;
import com.myf.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UsuarioRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(repository, passwordEncoder, jwtService);
    }

    @Test
    void registerPersistsEncodedPasswordAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Ana", "Pérez", "ANA@example.com", "clave-segura");
        when(repository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hash-seguro");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(42L);
            return usuario;
        });
        when(jwtService.create(any(Usuario.class))).thenReturn("jwt-de-prueba");

        AuthResponse response = service.register(request);

        assertThat(response.token()).isEqualTo("jwt-de-prueba");
        assertThat(response.userId()).isEqualTo(42L);
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.nombres()).isEqualTo("Ana");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hash-seguro");
        verify(passwordEncoder).encode(request.password());
        verify(jwtService).create(captor.getValue());
    }
}
