package com.myf.sales.repository;
import com.myf.sales.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ClienteRepository extends JpaRepository<Cliente,Long>{
 Optional<Cliente> findFirstByTelefono(String telefono);
 Optional<Cliente> findFirstByEmailIgnoreCase(String email);
 List<Cliente> findAllByOrderByFechaCreacionDesc();
}
