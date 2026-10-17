package com.myf.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProductoRequest(
        Long categoriaId,
        Long marcaId,
        @NotBlank String codigo,
        @NotBlank String nombre,
        String descripcion,
        @Pattern(regexp = "PERFUME|DECANT") String tipo,
        @DecimalMin("0.01") BigDecimal volumenMl,
        @NotBlank @Pattern(regexp = "HOMBRE|MUJER|UNISEX") String genero,
        @DecimalMin("0.0") BigDecimal precio,
        @DecimalMin("0.0") BigDecimal costo,
        String imagenUrl,
        List<@Valid PresentacionRequest> presentaciones
) {}
