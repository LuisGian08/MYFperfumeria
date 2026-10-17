package com.myf.product.repository;

import com.myf.product.entity.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @EntityGraph(attributePaths = {"categoria", "marca", "presentaciones"})
    List<Producto> findDistinctByEstadoOrderByNombreAsc(String estado);

    @Override
    @EntityGraph(attributePaths = {"categoria", "marca", "presentaciones"})
    Optional<Producto> findById(Long id);

    boolean existsByCodigoIgnoreCase(String codigo);
}
