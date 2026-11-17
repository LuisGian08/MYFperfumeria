package com.myf.sales.repository;

import com.myf.sales.entity.ReporteProductoVendido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReporteProductoRepository extends JpaRepository<ReporteProductoVendido, Long> {
    Optional<ReporteProductoVendido> findByFechaAndPresentacionId(LocalDate fecha, Long presentacionId);

    List<ReporteProductoVendido> findByFechaOrderByCantidadVendidaDesc(LocalDate fecha);

    List<ReporteProductoVendido> findTop10ByOrderByCantidadVendidaDesc();
}
