package com.myf.report.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="ReporteVentaDiaria")
public class ReporteVentaDiaria {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="Id") private Long id;
 @Column(name="Fecha",nullable=false,unique=true) private LocalDate fecha;
 @Column(name="CantidadVentas",nullable=false) private Integer cantidadVentas=0;
 @Column(name="UnidadesVendidas",nullable=false) private Integer unidadesVendidas=0;
 @Column(name="BotellasVendidas",nullable=false) private Integer botellasVendidas=0;
 @Column(name="DecantsVendidos",nullable=false) private Integer decantsVendidos=0;
 @Column(name="TotalVentas",nullable=false) private BigDecimal totalVentas=BigDecimal.ZERO;
 @Column(name="FechaActualizacion",nullable=false) private LocalDateTime fechaActualizacion=LocalDateTime.now();
 @PreUpdate void u(){fechaActualizacion=LocalDateTime.now();}
 public Long getId(){return id;} public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate v){fecha=v;}
 public Integer getCantidadVentas(){return cantidadVentas;} public void setCantidadVentas(Integer v){cantidadVentas=v;} public Integer getUnidadesVendidas(){return unidadesVendidas;} public void setUnidadesVendidas(Integer v){unidadesVendidas=v;}
 public Integer getBotellasVendidas(){return botellasVendidas;} public void setBotellasVendidas(Integer v){botellasVendidas=v;} public Integer getDecantsVendidos(){return decantsVendidos;} public void setDecantsVendidos(Integer v){decantsVendidos=v;}
 public BigDecimal getTotalVentas(){return totalVentas;} public void setTotalVentas(BigDecimal v){totalVentas=v;}
}
