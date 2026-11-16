package com.myf.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Correo de la cuenta", example = "admin@myf.local")
        @Email @NotBlank String email,
        @Schema(description = "Contraseña de la cuenta", example = "Admin123*")
        @NotBlank String password
) {}
