package com.myf.product.controller;

import com.myf.product.dto.EstadoProductoRequest;
import com.myf.product.service.ProductoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/productos")
@Tag(name = "Administración de productos", description = "Consulta administrativa de todos los productos, incluidos los inactivos.")
@SecurityRequirement(name = "bearerAuth")
public class ProductoAdministracionController {
    private final ProductoService service;

    public ProductoAdministracionController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar productos para administración")
    public Object listar() {
        return service.listarAdministracion();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar producto para administración")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.obtener(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
