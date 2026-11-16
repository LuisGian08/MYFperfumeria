package com.myf.sales.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
public record VentaRequest(
        @Schema(description = "ID del usuario que registra la venta administrativa", example = "1")
        Long usuarioId,
        @Schema(description = "Descuento monetario; en el checkout público se ignora", example = "0")
        BigDecimal descuento,
        @Schema(description = "Método de pago registrado", example = "YAPE")
        @NotBlank String metodoPago,
        String observacion,
        String canal,
        @Schema(description = "Datos del cliente; requeridos para el checkout público")
        @Valid ClienteRequest cliente,
        @Schema(description = "Artículos de la venta; se requieren al menos uno")
        @NotEmpty List<@Valid Item> items
){
 public record ClienteRequest(
         @Schema(description = "Nombre completo", example = "Ana Pérez") @NotBlank String nombre,
         @Schema(description = "Teléfono de contacto", example = "999123456") @NotBlank String telefono,
         @Schema(description = "Correo electrónico", example = "ana@example.com") @Email String email,
         @Schema(description = "Dirección de entrega", example = "Lima") String direccion
 ){}
 public record Item(
         @Schema(description = "ID del producto", example = "1") @NotNull Long productoId,
         @Schema(description = "ID de presentación; omitir para seleccionar botella por defecto", example = "2") Long presentacionId,
         @Schema(description = "Cantidad solicitada", example = "1") @Min(1) int cantidad
 ){}
}
