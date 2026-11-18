package com.myf.product.controller;

import com.myf.product.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProductoInternoController {
    private final ProductoService service;

    public ProductoInternoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping("/internal/api/productos/{id}")
    @Operation(hidden = true)
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.obtener(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
