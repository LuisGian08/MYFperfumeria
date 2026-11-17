package com.myf.sales.repository;

import com.myf.sales.entity.ReporteVentaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReporteVentaRepository extends JpaRepository<ReporteVentaDiaria, Long> {
    Optional<ReporteVentaDiaria> findByFecha(LocalDate fecha);
}
