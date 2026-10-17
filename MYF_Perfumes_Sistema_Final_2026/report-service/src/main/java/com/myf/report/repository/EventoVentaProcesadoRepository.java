package com.myf.report.repository;

import com.myf.report.entity.EventoVentaProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoVentaProcesadoRepository extends JpaRepository<EventoVentaProcesado,Long> {
    boolean existsByVentaId(Long ventaId);
}
