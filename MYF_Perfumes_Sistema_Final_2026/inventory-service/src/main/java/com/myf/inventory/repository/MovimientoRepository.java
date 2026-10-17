package com.myf.inventory.repository;
import com.myf.inventory.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovimientoRepository extends JpaRepository<MovimientoInventario,Long>{
    List<MovimientoInventario> findTop100ByOrderByFechaMovimientoDesc();
    List<MovimientoInventario> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);
}
