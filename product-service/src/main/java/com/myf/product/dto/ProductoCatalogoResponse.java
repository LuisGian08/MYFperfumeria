package com.myf.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoCatalogoResponse(
        Long id,
        String categoria,
        String marca,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String imagenUrl,
        List<Presentacion> presentaciones
) {
    public record Presentacion(
            Long id,
            String tipo,
            BigDecimal volumenMl,
            BigDecimal precio
    ) {}
}
