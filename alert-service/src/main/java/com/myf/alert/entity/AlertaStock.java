package com.myf.alert.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="AlertaStock")
public class AlertaStock {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="Id") private Long id;
 @Column(name="ProductoId",nullable=false) private Long productoId;
 @Column(name="Tipo",nullable=false) private String tipo;
 @Column(name="StockActual",nullable=false) private Integer stockActual;
 @Column(name="StockMinimo",nullable=false) private Integer stockMinimo;
 @Column(name="Unidad",nullable=false) private String unidad="UNIDAD";
 @Column(name="Mensaje",nullable=false) private String mensaje;
 @Column(name="Estado",nullable=false) private String estado="PENDIENTE";
 @Column(name="FechaAlerta",nullable=false) private LocalDateTime fechaAlerta=LocalDateTime.now();
 @Column(name="FechaAtendida") private LocalDateTime fechaAtendida;
 public Long getId(){return id;} public Long getProductoId(){return productoId;} public void setProductoId(Long v){productoId=v;}
 public String getTipo(){return tipo;} public void setTipo(String v){tipo=v;} public Integer getStockActual(){return stockActual;} public void setStockActual(Integer v){stockActual=v;}
 public Integer getStockMinimo(){return stockMinimo;} public void setStockMinimo(Integer v){stockMinimo=v;} public String getUnidad(){return unidad;} public void setUnidad(String v){unidad=v;}
 public String getMensaje(){return mensaje;} public void setMensaje(String v){mensaje=v;} public String getEstado(){return estado;} public void setEstado(String v){estado=v;}
 public LocalDateTime getFechaAlerta(){return fechaAlerta;} public LocalDateTime getFechaAtendida(){return fechaAtendida;} public void setFechaAtendida(LocalDateTime v){fechaAtendida=v;}
}
