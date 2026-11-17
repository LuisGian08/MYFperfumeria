package com.myf.sales.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ReporteProductoVendido")
public class ReporteProductoVendido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "ProductoId", nullable = false)
    private Long productoId;

    @Column(name = "PresentacionId")
    private Long presentacionId;

    @Column(name = "NombreProducto", nullable = false)
    private String nombreProducto;

    @Column(name = "TipoPresentacion", nullable = false)
    private String tipoPresentacion = "BOTELLA";

    @Column(name = "VolumenMl", precision = 10, scale = 2)
    private BigDecimal volumenMl;

    @Column(name = "CantidadVendida", nullable = false)
    private Integer cantidadVendida = 0;

    @Column(name = "TotalVendido", nullable = false)
    private BigDecimal totalVendido = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Long getPresentacionId() {
        return presentacionId;
    }

    public void setPresentacionId(Long presentacionId) {
        this.presentacionId = presentacionId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getTipoPresentacion() {
        return tipoPresentacion;
    }

    public void setTipoPresentacion(String tipoPresentacion) {
        this.tipoPresentacion = tipoPresentacion;
    }

    public BigDecimal getVolumenMl() {
        return volumenMl;
    }

    public void setVolumenMl(BigDecimal volumenMl) {
        this.volumenMl = volumenMl;
    }

    public Integer getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(Integer cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getTotalVendido() {
        return totalVendido;
    }

    public void setTotalVendido(BigDecimal totalVendido) {
        this.totalVendido = totalVendido;
    }
}
