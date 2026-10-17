package com.myf.sales.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "Venta")
public class Venta {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "Id")
 private Long id;

 @Column(name = "NumeroVenta", nullable = false, unique = true)
 private String numeroVenta;

 @Column(name = "UsuarioId")
 private Long usuarioId;

 @Column(name = "ClienteId")
 private Long clienteId;

 @Column(name = "FechaVenta", nullable = false)
 private LocalDateTime fechaVenta = LocalDateTime.now();

 @Column(name = "Subtotal", nullable = false)
 private BigDecimal subtotal = BigDecimal.ZERO;

 @Column(name = "Descuento", nullable = false)
 private BigDecimal descuento = BigDecimal.ZERO;

 @Column(name = "Total", nullable = false)
 private BigDecimal total = BigDecimal.ZERO;

 @Column(name = "MetodoPago", nullable = false)
 private String metodoPago;

 // PENDIENTE / PAGADO
 @Column(name = "EstadoPago", nullable = false)
 private String estadoPago = "PENDIENTE";

 // DELIVERY / RECOJO
 @Column(name = "TipoEntrega", nullable = false)
 private String tipoEntrega = "RECOJO";

 // URL DEL COMPROBANTE DE PAGO
 @Column(name = "ComprobanteUrl", length = 500)
 private String comprobanteUrl;

 @Column(name = "Estado", nullable = false)
 private String estado = "REGISTRADA";

 @Column(name = "Canal", nullable = false)
 private String canal = "ADMIN";

 @Column(name = "Observacion", length = 800)
 private String observacion;

 @Column(name = "StockReintegrado", nullable = false)
 private Boolean stockReintegrado = false;

 @Column(name = "FechaCreacion", nullable = false)
 private LocalDateTime fechaCreacion = LocalDateTime.now();

 @OneToMany(
         mappedBy = "venta",
         cascade = CascadeType.ALL,
         orphanRemoval = true,
         fetch = FetchType.EAGER
 )
 private List<DetalleVenta> detalles = new ArrayList<>();


 public Long getId() {
  return id;
 }

 public String getNumeroVenta() {
  return numeroVenta;
 }

 public void setNumeroVenta(String numeroVenta) {
  this.numeroVenta = numeroVenta;
 }

 public Long getUsuarioId() {
  return usuarioId;
 }

 public void setUsuarioId(Long usuarioId) {
  this.usuarioId = usuarioId;
 }

 public Long getClienteId() {
  return clienteId;
 }

 public void setClienteId(Long clienteId) {
  this.clienteId = clienteId;
 }

 public LocalDateTime getFechaVenta() {
  return fechaVenta;
 }

 public BigDecimal getSubtotal() {
  return subtotal;
 }

 public void setSubtotal(BigDecimal subtotal) {
  this.subtotal = subtotal;
 }

 public BigDecimal getDescuento() {
  return descuento;
 }

 public void setDescuento(BigDecimal descuento) {
  this.descuento = descuento;
 }

 public BigDecimal getTotal() {
  return total;
 }

 public void setTotal(BigDecimal total) {
  this.total = total;
 }

 public String getMetodoPago() {
  return metodoPago;
 }

 public void setMetodoPago(String metodoPago) {
  this.metodoPago = metodoPago;
 }

 public String getEstadoPago() {
  return estadoPago;
 }

 public void setEstadoPago(String estadoPago) {
  this.estadoPago = estadoPago;
 }

 public String getTipoEntrega() {
  return tipoEntrega;
 }

 public void setTipoEntrega(String tipoEntrega) {
  this.tipoEntrega = tipoEntrega;
 }

 public String getComprobanteUrl() {
  return comprobanteUrl;
 }

 public void setComprobanteUrl(String comprobanteUrl) {
  this.comprobanteUrl = comprobanteUrl;
 }

 public String getEstado() {
  return estado;
 }

 public void setEstado(String estado) {
  this.estado = estado;
 }

 public String getCanal() {
  return canal;
 }

 public void setCanal(String canal) {
  this.canal = canal;
 }

 public String getObservacion() {
  return observacion;
 }

 public void setObservacion(String observacion) {
  this.observacion = observacion;
 }

 public Boolean getStockReintegrado() {
  return stockReintegrado;
 }

 public void setStockReintegrado(Boolean stockReintegrado) {
  this.stockReintegrado = stockReintegrado;
 }

 public List<DetalleVenta> getDetalles() {
  return detalles;
 }

 public void addDetalle(DetalleVenta detalle) {
  detalle.setVenta(this);
  detalles.add(detalle);
 }
}