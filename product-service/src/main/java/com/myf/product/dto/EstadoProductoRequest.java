package com.myf.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EstadoProductoRequest(
        @NotBlank
        @Pattern(regexp = "(?i)ACTIVO|INACTIVO")
        String estado
) {}
