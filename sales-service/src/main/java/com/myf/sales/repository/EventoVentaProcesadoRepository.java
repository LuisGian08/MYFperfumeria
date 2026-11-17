package com.myf.sales.repository;

import com.myf.sales.entity.EventoVentaProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoVentaProcesadoRepository extends JpaRepository<EventoVentaProcesado, Long> {
    boolean existsByVentaId(Long ventaId);
}
