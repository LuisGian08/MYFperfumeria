package com.myf.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Inventario")
public class Inventario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "Id") private Long id;
    @Column(name = "ProductoId", nullable = false, unique = true) private Long productoId;
    @Column(name = "StockActual", nullable = false) private Integer stockActual = 0;
    @Column(name = "StockMinimo", nullable = false) private Integer stockMinimo = 2;
    @Column(name = "VolumenDecantMl", nullable = false) private Integer volumenDecantMl = 0;
    @Column(name = "VolumenDecantMinimoMl", nullable = false) private Integer volumenDecantMinimoMl = 20;
    @Version @Column(name = "Version", nullable = false) private Long version = 0L;
    @Column(name = "FechaActualizacion", nullable = false) private LocalDateTime fechaActualizacion = LocalDateTime.now();
    @PreUpdate void update() { fechaActualizacion = LocalDateTime.now(); }
    public Long getId(){return id;} public Long getProductoId(){return productoId;} public void setProductoId(Long v){productoId=v;}
    public Integer getStockActual(){return stockActual;} public void setStockActual(Integer v){stockActual=v;}
    public Integer getStockMinimo(){return stockMinimo;} public void setStockMinimo(Integer v){stockMinimo=v;}
    public Integer getVolumenDecantMl(){return volumenDecantMl;} public void setVolumenDecantMl(Integer v){volumenDecantMl=v;}
    public Integer getVolumenDecantMinimoMl(){return volumenDecantMinimoMl;} public void setVolumenDecantMinimoMl(Integer v){volumenDecantMinimoMl=v;}
    public Long getVersion(){return version;}
    public LocalDateTime getFechaActualizacion(){return fechaActualizacion;}
}
