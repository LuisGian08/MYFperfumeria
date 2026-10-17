package com.myf.report.repository;
import com.myf.report.entity.ReporteProductoVendido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface ReporteProductoRepository extends JpaRepository<ReporteProductoVendido,Long>{
 Optional<ReporteProductoVendido> findByFechaAndPresentacionId(LocalDate fecha,Long presentacionId);
 List<ReporteProductoVendido> findByFechaOrderByCantidadVendidaDesc(LocalDate fecha);
 List<ReporteProductoVendido> findTop10ByOrderByCantidadVendidaDesc();
}
