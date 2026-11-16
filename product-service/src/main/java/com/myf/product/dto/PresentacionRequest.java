package com.myf.product.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record PresentacionRequest(
        String codigo,
        @Schema(description = "Tipo de presentación", example = "DECANT", allowableValues = {"BOTELLA", "DECANT"})
        @NotBlank @Pattern(regexp = "BOTELLA|DECANT") String tipo,
        @Schema(description = "Volumen en mililitros", example = "10")
        @NotNull @DecimalMin("0.01") BigDecimal volumenMl,
        @Schema(description = "Precio de venta", example = "35.00")
        @NotNull @DecimalMin("0.0") BigDecimal precio,
        @DecimalMin("0.0") BigDecimal costo,
        @Schema(description = "Indica si se crea activa", example = "true")
        Boolean activo
) {}
