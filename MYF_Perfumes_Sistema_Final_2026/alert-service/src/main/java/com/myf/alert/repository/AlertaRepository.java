package com.myf.alert.repository;
import com.myf.alert.entity.AlertaStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AlertaRepository extends JpaRepository<AlertaStock,Long>{
 List<AlertaStock> findByEstadoOrderByFechaAlertaDesc(String estado);
 Optional<AlertaStock> findFirstByProductoIdAndUnidadAndEstadoOrderByFechaAlertaDesc(Long productoId,String unidad,String estado);
}
