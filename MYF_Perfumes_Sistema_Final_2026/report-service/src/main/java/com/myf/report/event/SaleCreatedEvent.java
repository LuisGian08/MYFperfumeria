package com.myf.report.event;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record SaleCreatedEvent(Long ventaId,String numeroVenta,LocalDate fecha,BigDecimal total,List<Item> items){
 public record Item(Long productoId,Long presentacionId,String nombreProducto,String tipoPresentacion,BigDecimal volumenMl,int cantidad,BigDecimal total){}
}
