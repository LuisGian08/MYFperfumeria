package com.myf.product.repository;

import com.myf.product.entity.PresentacionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PresentacionProductoRepository extends JpaRepository<PresentacionProducto, Long> {
    List<PresentacionProducto> findByProductoIdAndEstadoOrderByVolumenMlAsc(Long productoId, String estado);
    boolean existsByCodigoIgnoreCase(String codigo);
}
