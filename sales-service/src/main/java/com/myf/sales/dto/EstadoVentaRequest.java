package com.myf.sales.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
public record EstadoVentaRequest(
        @Schema(description = "Nuevo estado del pedido", example = "CONFIRMADO",
                allowableValues = {"PENDIENTE", "CONFIRMADO", "PREPARANDO", "LISTO", "ENTREGADO", "CANCELADO", "REGISTRADA"})
        @NotBlank String estado
) {}
