package com.myf.sales.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
public record VentaRequest(
        Long usuarioId,
        BigDecimal descuento,
        @NotBlank String metodoPago,
        String observacion,
        String canal,
        @Valid ClienteRequest cliente,
        @NotEmpty List<@Valid Item> items
){
 public record ClienteRequest(@NotBlank String nombre,@NotBlank String telefono,@Email String email,String direccion){}
 public record Item(@NotNull Long productoId,Long presentacionId,@Min(1) int cantidad){}
}
