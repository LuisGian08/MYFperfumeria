package com.myf.inventory.repository;

import com.myf.inventory.entity.AlertaStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<AlertaStock, Long> {
    List<AlertaStock> findByEstadoOrderByFechaAlertaDesc(String estado);

    Optional<AlertaStock> findFirstByProductoIdAndUnidadAndEstadoOrderByFechaAlertaDesc(
            Long productoId,
            String unidad,
            String estado);
}
