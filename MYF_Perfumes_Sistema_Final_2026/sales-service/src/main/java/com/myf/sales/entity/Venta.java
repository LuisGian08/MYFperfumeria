package com.myf.sales.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Entity @Table(name="Venta")
public class Venta {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="Id") private Long id;
 @Column(name="NumeroVenta",nullable=false,unique=true) private String numeroVenta;
 @Column(name="UsuarioId") private Long usuarioId;
 @Column(name="ClienteId") private Long clienteId;
 @Column(name="FechaVenta",nullable=false) private LocalDateTime fechaVenta=LocalDateTime.now();
 @Column(name="Subtotal",nullable=false) private BigDecimal subtotal=BigDecimal.ZERO;
 @Column(name="Descuento",nullable=false) private BigDecimal descuento=BigDecimal.ZERO;
 @Column(name="Total",nullable=false) private BigDecimal total=BigDecimal.ZERO;
 @Column(name="MetodoPago",nullable=false) private String metodoPago;
 @Column(name="Estado",nullable=false) private String estado="REGISTRADA";
 @Column(name="Canal",nullable=false) private String canal="ADMIN";
 @Column(name="Observacion",length=800) private String observacion;
 @Column(name="StockReintegrado",nullable=false) private Boolean stockReintegrado=false;
 @Column(name="FechaCreacion",nullable=false) private LocalDateTime fechaCreacion=LocalDateTime.now();
 @OneToMany(mappedBy="venta",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER) private List<DetalleVenta> detalles=new ArrayList<>();
 public Long getId(){return id;} public String getNumeroVenta(){return numeroVenta;} public void setNumeroVenta(String v){numeroVenta=v;}
 public Long getUsuarioId(){return usuarioId;} public void setUsuarioId(Long v){usuarioId=v;} public Long getClienteId(){return clienteId;} public void setClienteId(Long v){clienteId=v;}
 public LocalDateTime getFechaVenta(){return fechaVenta;} public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;} public BigDecimal getDescuento(){return descuento;} public void setDescuento(BigDecimal v){descuento=v;}
 public BigDecimal getTotal(){return total;} public void setTotal(BigDecimal v){total=v;} public String getMetodoPago(){return metodoPago;} public void setMetodoPago(String v){metodoPago=v;}
 public String getEstado(){return estado;} public void setEstado(String v){estado=v;} public String getCanal(){return canal;} public void setCanal(String v){canal=v;}
 public String getObservacion(){return observacion;} public void setObservacion(String v){observacion=v;} public Boolean getStockReintegrado(){return stockReintegrado;} public void setStockReintegrado(Boolean v){stockReintegrado=v;}
 public List<DetalleVenta> getDetalles(){return detalles;} public void addDetalle(DetalleVenta d){d.setVenta(this);detalles.add(d);}
}
