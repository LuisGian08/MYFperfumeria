package com.myf.report.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EventoVentaProcesado", uniqueConstraints = @UniqueConstraint(columnNames = "VentaId"))
public class EventoVentaProcesado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "Id") private Long id;
    @Column(name = "VentaId", nullable = false, unique = true) private Long ventaId;
    @Column(name = "FechaProcesado", nullable = false) private LocalDateTime fechaProcesado = LocalDateTime.now();
    public Long getId(){return id;}
    public Long getVentaId(){return ventaId;}
    public void setVentaId(Long ventaId){this.ventaId=ventaId;}
    public LocalDateTime getFechaProcesado(){return fechaProcesado;}
}
