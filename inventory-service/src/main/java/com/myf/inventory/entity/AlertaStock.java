package com.myf.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "AlertaStock")
public class AlertaStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "ProductoId", nullable = false)
    private Long productoId;

    @Column(name = "Tipo", nullable = false)
    private String tipo;

    @Column(name = "StockActual", nullable = false)
    private Integer stockActual;

    @Column(name = "StockMinimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "Unidad", nullable = false)
    private String unidad = "UNIDAD";

    @Column(name = "Mensaje", nullable = false)
    private String mensaje;

    @Column(name = "Estado", nullable = false)
    private String estado = "PENDIENTE";

    @Column(name = "FechaAlerta", nullable = false)
    private LocalDateTime fechaAlerta = LocalDateTime.now();

    @Column(name = "FechaAtendida")
    private LocalDateTime fechaAtendida;

    public Long getId() {
        return id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAlerta() {
        return fechaAlerta;
    }

    public LocalDateTime getFechaAtendida() {
        return fechaAtendida;
    }

    public void setFechaAtendida(LocalDateTime fechaAtendida) {
        this.fechaAtendida = fechaAtendida;
    }
}
