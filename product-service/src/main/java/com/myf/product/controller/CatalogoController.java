package com.myf.product.controller;

import com.myf.product.entity.*;
import com.myf.product.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class CatalogoController {
    private final CategoriaRepository categorias;
    private final MarcaRepository marcas;

    public CatalogoController(CategoriaRepository categorias, MarcaRepository marcas) {
        this.categorias = categorias;
        this.marcas = marcas;
    }

    @GetMapping("/api/categorias")
    public List<Categoria> categorias() { return categorias.findAll(); }

    @PostMapping("/api/categorias")
    public Categoria crearCategoria(@RequestBody Categoria x) { return categorias.save(x); }

    @PutMapping("/api/categorias/{id}")
    public ResponseEntity<?> editarCategoria(@PathVariable Long id, @RequestBody Categoria x) {
        return categorias.findById(id).<ResponseEntity<?>>map(actual -> {
            actual.setNombre(x.getNombre());
            actual.setDescripcion(x.getDescripcion());
            actual.setEstado(x.getEstado() == null ? actual.getEstado() : x.getEstado());
            return ResponseEntity.ok(categorias.save(actual));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/marcas")
    public List<Marca> marcas() { return marcas.findAll(); }

    @PostMapping("/api/marcas")
    public Marca crearMarca(@RequestBody Marca x) { return marcas.save(x); }

    @PutMapping("/api/marcas/{id}")
    public ResponseEntity<?> editarMarca(@PathVariable Long id, @RequestBody Marca x) {
        return marcas.findById(id).<ResponseEntity<?>>map(actual -> {
            actual.setNombre(x.getNombre());
            actual.setPaisOrigen(x.getPaisOrigen());
            actual.setEstado(x.getEstado() == null ? actual.getEstado() : x.getEstado());
            return ResponseEntity.ok(marcas.save(actual));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
