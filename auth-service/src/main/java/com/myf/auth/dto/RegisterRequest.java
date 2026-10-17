package com.myf.auth.dto;
import jakarta.validation.constraints.Email; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.Size;
public record RegisterRequest(@NotBlank String nombres,@NotBlank String apellidos,@Email @NotBlank String email,@Size(min=6) String password) {}
