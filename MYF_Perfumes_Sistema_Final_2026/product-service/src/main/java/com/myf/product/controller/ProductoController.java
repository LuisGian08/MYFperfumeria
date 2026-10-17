package com.myf.product.controller;

import com.myf.product.dto.ProductoRequest;
import com.myf.product.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService service;
    public ProductoController(ProductoService service) { this.service = service; }

    @GetMapping
    public Object listar() { return service.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try { return ResponseEntity.ok(service.obtener(id)); }
        catch (Exception e) { return ResponseEntity.status(404).body(Map.of("error", e.getMessage())); }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoRequest r) {
        try { return ResponseEntity.status(201).body(service.crear(r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest r) {
        try { return ResponseEntity.ok(service.actualizar(id, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrar(@PathVariable Long id) {
        try { service.desactivar(id); return ResponseEntity.noContent().build(); }
        catch (Exception e) { return ResponseEntity.status(404).body(Map.of("error", e.getMessage())); }
    }
}
