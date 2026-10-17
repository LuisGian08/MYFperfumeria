package com.myf.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MovimientoInventario")
public class MovimientoInventario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "Id") private Long id;
    @Column(name = "ProductoId", nullable = false) private Long productoId;
    @Column(name = "TipoMovimiento", nullable = false) private String tipoMovimiento;
    @Column(name = "Cantidad", nullable = false) private Integer cantidad;
    @Column(name = "Unidad", nullable = false) private String unidad = "UNIDAD";
    @Column(name = "Motivo") private String motivo;
    @Column(name = "ReferenciaVentaId") private Long referenciaVentaId;
    @Column(name = "UsuarioId") private Long usuarioId;
    @Column(name = "FechaMovimiento", nullable = false) private LocalDateTime fechaMovimiento = LocalDateTime.now();
    public Long getId(){return id;} public Long getProductoId(){return productoId;} public void setProductoId(Long v){productoId=v;}
    public String getTipoMovimiento(){return tipoMovimiento;} public void setTipoMovimiento(String v){tipoMovimiento=v;}
    public Integer getCantidad(){return cantidad;} public void setCantidad(Integer v){cantidad=v;}
    public String getUnidad(){return unidad;} public void setUnidad(String v){unidad=v;}
    public String getMotivo(){return motivo;} public void setMotivo(String v){motivo=v;}
    public Long getReferenciaVentaId(){return referenciaVentaId;} public void setReferenciaVentaId(Long v){referenciaVentaId=v;}
    public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;}
    public LocalDateTime getFechaMovimiento(){return fechaMovimiento;}
}
