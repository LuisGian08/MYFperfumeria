package com.myf.sales.dto;
import java.math.BigDecimal;
import java.util.List;
public record ProductoDto(Long id,Long categoriaId,String categoria,Long marcaId,String marca,String codigo,String nombre,String descripcion,String genero,String imagenUrl,String estado,String tipo,BigDecimal volumenMl,BigDecimal precio,BigDecimal costo,List<PresentacionDto> presentaciones){
 public record PresentacionDto(Long id,String codigo,String tipo,BigDecimal volumenMl,BigDecimal precio,BigDecimal costo,String estado){}
}
