package com.myf.inventory.dto;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;
public record StockConfigRequest(
        @Schema(description = "Botellas cerradas disponibles", example = "5")
        @Min(0) Integer stockActual,
        @Schema(description = "Umbral de alerta de botellas", example = "2")
        @Min(0) Integer stockMinimo,
        @Schema(description = "Mililitros disponibles para decants", example = "120")
        @Min(0) Integer volumenDecantMl,
        @Schema(description = "Umbral de alerta de volumen en ml", example = "20")
        @Min(0) Integer volumenDecantMinimoMl
) {}
