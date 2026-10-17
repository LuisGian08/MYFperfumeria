package com.myf.inventory.controller;

import com.myf.inventory.dto.*;
import com.myf.inventory.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final InventarioService service;
    public InventarioController(InventarioService service) { this.service = service; }

    @GetMapping public Object listar() { return service.listar(); }
    @GetMapping("/movimientos") public Object movimientos(@RequestParam(required = false) Long productoId) {
        return productoId == null ? service.movimientos() : service.movimientos(productoId);
    }

    @GetMapping("/{productoId}")
    public ResponseEntity<?> get(@PathVariable Long productoId) {
        try { return ResponseEntity.ok(service.obtener(productoId)); }
        catch (Exception e) { return ResponseEntity.status(404).body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/configurar")
    public ResponseEntity<?> configurar(@PathVariable Long productoId, @Valid @RequestBody StockConfigRequest r) {
        try { return ResponseEntity.ok(service.configurar(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/entrada")
    public ResponseEntity<?> entrada(@PathVariable Long productoId, @Valid @RequestBody MovimientoRequest r) {
        try { return ResponseEntity.ok(service.entrada(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/salida")
    public ResponseEntity<?> salida(@PathVariable Long productoId, @Valid @RequestBody MovimientoRequest r) {
        try { return ResponseEntity.ok(service.salida(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/decant/entrada")
    public ResponseEntity<?> entradaDecant(@PathVariable Long productoId, @Valid @RequestBody MovimientoDecantRequest r) {
        try { return ResponseEntity.ok(service.entradaDecant(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/decant/salida")
    public ResponseEntity<?> salidaDecant(@PathVariable Long productoId, @Valid @RequestBody MovimientoDecantRequest r) {
        try { return ResponseEntity.ok(service.salidaDecant(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/decant/abrir")
    public ResponseEntity<?> abrirBotellaParaDecants(@PathVariable Long productoId, @Valid @RequestBody AbrirBotellaRequest r) {
        try { return ResponseEntity.ok(service.abrirBotellaParaDecants(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}
