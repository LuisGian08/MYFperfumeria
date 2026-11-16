package com.myf.inventory.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
public record MovimientoDecantRequest(
        @Schema(description = "Volumen del movimiento en mililitros", example = "10") @Min(1) int mililitros,
        @Schema(description = "Motivo del movimiento", example = "Ajuste de volumen") String motivo,
        @Schema(description = "Usuario que registra el movimiento", example = "1") Long usuarioId,
        @Schema(description = "Venta relacionada, si aplica") Long referenciaVentaId
) {}
