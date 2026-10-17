package com.myf.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PresentacionRequest(
        String codigo,
        @NotBlank @Pattern(regexp = "BOTELLA|DECANT") String tipo,
        @NotNull @DecimalMin("0.01") BigDecimal volumenMl,
        @NotNull @DecimalMin("0.0") BigDecimal precio,
        @DecimalMin("0.0") BigDecimal costo,
        Boolean activo
) {}
