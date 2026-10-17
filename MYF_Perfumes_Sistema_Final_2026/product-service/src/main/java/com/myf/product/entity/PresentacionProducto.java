package com.myf.product.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PresentacionProducto")
public class PresentacionProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProductoId", nullable = false)
    private Producto producto;

    @Column(name = "Codigo", nullable = false, unique = true, length = 80)
    private String codigo;

    @Column(name = "Tipo", nullable = false, length = 20)
    private String tipo; // BOTELLA | DECANT

    @Column(name = "VolumenMl", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumenMl;

    @Column(name = "Precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "Costo", precision = 12, scale = 2)
    private BigDecimal costo = BigDecimal.ZERO;

    @Column(name = "Estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "FechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "FechaActualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PreUpdate
    void preUpdate() { fechaActualizacion = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getVolumenMl() { return volumenMl; }
    public void setVolumenMl(BigDecimal volumenMl) { this.volumenMl = volumenMl; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
