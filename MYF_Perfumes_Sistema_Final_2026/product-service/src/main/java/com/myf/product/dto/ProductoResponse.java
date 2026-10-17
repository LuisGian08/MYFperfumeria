package com.myf.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoResponse(
        Long id,
        Long categoriaId,
        String categoria,
        Long marcaId,
        String marca,
        String codigo,
        String nombre,
        String descripcion,
        String genero,
        String imagenUrl,
        String estado,
        String tipo,
        BigDecimal volumenMl,
        BigDecimal precio,
        BigDecimal costo,
        List<PresentacionResponse> presentaciones
) {
    public record PresentacionResponse(
            Long id,
            String codigo,
            String tipo,
            BigDecimal volumenMl,
            BigDecimal precio,
            BigDecimal costo,
            String estado
    ) {}
}
