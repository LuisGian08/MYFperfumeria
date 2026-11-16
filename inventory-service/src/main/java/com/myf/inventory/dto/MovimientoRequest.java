package com.myf.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record MovimientoRequest(
        @Schema(description = "Cantidad de botellas", example = "2") @Min(1) int cantidad,
        @Schema(description = "Motivo del movimiento", example = "Reposición") String motivo,
        @Schema(description = "Usuario que registra el movimiento", example = "1") Long usuarioId,
        @Schema(description = "Venta relacionada, si aplica") Long referenciaVentaId
) {}