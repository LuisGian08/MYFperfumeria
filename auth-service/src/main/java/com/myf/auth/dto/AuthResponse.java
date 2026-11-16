package com.myf.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "Token JWT para el encabezado Authorization", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBteWYubG9jYWwifQ.signature")
        String token,
        @Schema(description = "Identificador del usuario", example = "1")
        Long userId,
        @Schema(description = "Correo autenticado", example = "admin@myf.local")
        String email,
        @Schema(description = "Nombre del usuario", example = "Administrador")
        String nombres
) {}
