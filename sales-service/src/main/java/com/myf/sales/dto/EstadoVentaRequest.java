package com.myf.sales.dto;
import jakarta.validation.constraints.NotBlank;
public record EstadoVentaRequest(@NotBlank String estado) {}
