package com.myf.inventory.dto;

import jakarta.validation.constraints.Min;

public record AbrirBotellaRequest(
        @Min(1) int mililitros,
        String motivo,
        Long usuarioId
) {}
