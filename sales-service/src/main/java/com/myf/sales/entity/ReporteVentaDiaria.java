package com.myf.sales.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ReporteVentaDiaria", uniqueConstraints = @UniqueConstraint(columnNames = "Fecha"))
public class ReporteVentaDiaria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Fecha", nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "CantidadVentas", nullable = false)
    private Integer cantidadVentas = 0;

    @Column(name = "UnidadesVendidas", nullable = false)
    private Integer unidadesVendidas = 0;

    @Column(name = "BotellasVendidas", nullable = false)
    private Integer botellasVendidas = 0;

    @Column(name = "DecantsVendidos", nullable = false)
    private Integer decantsVendidos = 0;

    @Column(name = "TotalVentas", nullable = false)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(name = "FechaActualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PreUpdate
    void actualizarFecha() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(Integer cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public Integer getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public void setUnidadesVendidas(Integer unidadesVendidas) {
        this.unidadesVendidas = unidadesVendidas;
    }

    public Integer getBotellasVendidas() {
        return botellasVendidas;
    }

    public void setBotellasVendidas(Integer botellasVendidas) {
        this.botellasVendidas = botellasVendidas;
    }

    public Integer getDecantsVendidos() {
        return decantsVendidos;
    }

    public void setDecantsVendidos(Integer decantsVendidos) {
        this.decantsVendidos = decantsVendidos;
    }

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }
}
