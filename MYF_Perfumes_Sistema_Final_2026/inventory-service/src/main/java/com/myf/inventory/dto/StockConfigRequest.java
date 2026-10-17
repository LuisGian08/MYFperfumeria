package com.myf.inventory.dto;
import jakarta.validation.constraints.Min;
public record StockConfigRequest(
        @Min(0) Integer stockActual,
        @Min(0) Integer stockMinimo,
        @Min(0) Integer volumenDecantMl,
        @Min(0) Integer volumenDecantMinimoMl
) {}
