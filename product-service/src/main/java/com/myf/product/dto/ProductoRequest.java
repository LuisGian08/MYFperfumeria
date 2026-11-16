package com.myf.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

public record ProductoRequest(
        @Schema(description = "Identificador de categoría", example = "1")
        Long categoriaId,
        @Schema(description = "Identificador de marca", example = "1")
        Long marcaId,
        @Schema(description = "Código único del producto", example = "MYF-001")
        @NotBlank String codigo,
        @Schema(description = "Nombre del perfume", example = "Perfume de muestra")
        @NotBlank String nombre,
        String descripcion,
        @Schema(description = "Tipo del producto principal", example = "PERFUME", allowableValues = {"PERFUME", "DECANT"})
        @Pattern(regexp = "PERFUME|DECANT") String tipo,
        @Schema(description = "Volumen de la presentación principal en ml", example = "100")
        @DecimalMin("0.01") BigDecimal volumenMl,
        @Schema(description = "Público objetivo", example = "UNISEX", allowableValues = {"HOMBRE", "MUJER", "UNISEX"})
        @NotBlank @Pattern(regexp = "HOMBRE|MUJER|UNISEX") String genero,
        @Schema(description = "Precio de la presentación principal", example = "250.00")
        @DecimalMin("0.0") BigDecimal precio,
        @DecimalMin("0.0") BigDecimal costo,
        String imagenUrl,
        @Schema(description = "Presentaciones activas asociadas al producto")
        List<@Valid PresentacionRequest> presentaciones
) {}
