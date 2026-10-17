package com.myf.report.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="ReporteProductoVendido")
public class ReporteProductoVendido {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="Id") private Long id;
 @Column(name="Fecha",nullable=false) private LocalDate fecha;
 @Column(name="ProductoId",nullable=false) private Long productoId;
 @Column(name="PresentacionId") private Long presentacionId;
 @Column(name="NombreProducto",nullable=false) private String nombreProducto;
 @Column(name="TipoPresentacion",nullable=false) private String tipoPresentacion="BOTELLA";
 @Column(name="VolumenMl",precision=10,scale=2) private BigDecimal volumenMl;
 @Column(name="CantidadVendida",nullable=false) private Integer cantidadVendida=0;
 @Column(name="TotalVendido",nullable=false) private BigDecimal totalVendido=BigDecimal.ZERO;
 public Long getId(){return id;} public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate v){fecha=v;} public Long getProductoId(){return productoId;} public void setProductoId(Long v){productoId=v;}
 public Long getPresentacionId(){return presentacionId;} public void setPresentacionId(Long v){presentacionId=v;} public String getNombreProducto(){return nombreProducto;} public void setNombreProducto(String v){nombreProducto=v;}
 public String getTipoPresentacion(){return tipoPresentacion;} public void setTipoPresentacion(String v){tipoPresentacion=v;} public BigDecimal getVolumenMl(){return volumenMl;} public void setVolumenMl(BigDecimal v){volumenMl=v;}
 public Integer getCantidadVendida(){return cantidadVendida;} public void setCantidadVendida(Integer v){cantidadVendida=v;} public BigDecimal getTotalVendido(){return totalVendido;} public void setTotalVendido(BigDecimal v){totalVendido=v;}
}
