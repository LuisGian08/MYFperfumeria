package com.myf.product.controller;

import com.myf.product.entity.*;
import com.myf.product.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Tag(name = "Marcas y categorías", description = "Consulta pública y administración del catálogo auxiliar.")
public class CatalogoController {
    private final CategoriaRepository categorias;
    private final MarcaRepository marcas;

    public CatalogoController(CategoriaRepository categorias, MarcaRepository marcas) {
        this.categorias = categorias;
        this.marcas = marcas;
    }

    @GetMapping("/api/categorias")
    @Operation(summary = "Listar categorías", description = "Devuelve las categorías registradas; no requiere autenticación.")
    public List<Categoria> categorias() { return categorias.findAll(); }

    @PostMapping("/api/categorias")
    @Operation(summary = "Crear categoría", security = @SecurityRequirement(name = "bearerAuth"))
    public Categoria crearCategoria(@RequestBody Categoria x) { return categorias.save(x); }

    @PutMapping("/api/categorias/{id}")
    @Operation(summary = "Actualizar categoría", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> editarCategoria(@PathVariable Long id, @RequestBody Categoria x) {
        return categorias.findById(id).<ResponseEntity<?>>map(actual -> {
            actual.setNombre(x.getNombre());
            actual.setDescripcion(x.getDescripcion());
            actual.setEstado(x.getEstado() == null ? actual.getEstado() : x.getEstado());
            return ResponseEntity.ok(categorias.save(actual));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/marcas")
    @Operation(summary = "Listar marcas", description = "Devuelve las marcas registradas; no requiere autenticación.")
    public List<Marca> marcas() { return marcas.findAll(); }

    @PostMapping("/api/marcas")
    @Operation(summary = "Crear marca", security = @SecurityRequirement(name = "bearerAuth"))
    public Marca crearMarca(@RequestBody Marca x) { return marcas.save(x); }

    @PutMapping("/api/marcas/{id}")
    @Operation(summary = "Actualizar marca", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> editarMarca(@PathVariable Long id, @RequestBody Marca x) {
        return marcas.findById(id).<ResponseEntity<?>>map(actual -> {
            actual.setNombre(x.getNombre());
            actual.setPaisOrigen(x.getPaisOrigen());
            actual.setEstado(x.getEstado() == null ? actual.getEstado() : x.getEstado());
            return ResponseEntity.ok(marcas.save(actual));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
