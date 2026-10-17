package com.myf.sales.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="DetalleVenta")
public class DetalleVenta {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="Id") private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="VentaId",nullable=false) private Venta venta;
 @Column(name="ProductoId",nullable=false) private Long productoId;
 @Column(name="PresentacionId") private Long presentacionId;
 @Column(name="NombreProducto",nullable=false) private String nombreProducto;
 @Column(name="TipoProducto",nullable=false) private String tipoProducto;
 @Column(name="VolumenMl",precision=10,scale=2) private BigDecimal volumenMl;
 @Column(name="Cantidad",nullable=false) private Integer cantidad;
 @Column(name="PrecioUnitario",nullable=false) private BigDecimal precioUnitario;
 @Column(name="Subtotal",nullable=false) private BigDecimal subtotal;
 public Long getId(){return id;} public void setVenta(Venta v){venta=v;} public Long getProductoId(){return productoId;} public void setProductoId(Long v){productoId=v;}
 public Long getPresentacionId(){return presentacionId;} public void setPresentacionId(Long v){presentacionId=v;} public String getNombreProducto(){return nombreProducto;} public void setNombreProducto(String v){nombreProducto=v;}
 public String getTipoProducto(){return tipoProducto;} public void setTipoProducto(String v){tipoProducto=v;} public BigDecimal getVolumenMl(){return volumenMl;} public void setVolumenMl(BigDecimal v){volumenMl=v;}
 public Integer getCantidad(){return cantidad;} public void setCantidad(Integer v){cantidad=v;} public BigDecimal getPrecioUnitario(){return precioUnitario;} public void setPrecioUnitario(BigDecimal v){precioUnitario=v;}
 public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;}
}
