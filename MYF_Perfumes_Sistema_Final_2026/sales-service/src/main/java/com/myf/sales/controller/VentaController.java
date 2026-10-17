package com.myf.sales.controller;
import com.myf.sales.dto.*;
import com.myf.sales.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/ventas")
public class VentaController {
 private final VentaService service; public VentaController(VentaService service){this.service=service;}
 @GetMapping public Object listar(){return service.listar();}
 @GetMapping("/{id}") public ResponseEntity<?> get(@PathVariable Long id){try{return ResponseEntity.ok(service.obtener(id));}catch(Exception e){return ResponseEntity.status(404).body(Map.of("error",e.getMessage()));}}
 @PostMapping public ResponseEntity<?> crear(@Valid @RequestBody VentaRequest r){try{return ResponseEntity.status(201).body(service.crear(r,false));}catch(Exception e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}}
 @PostMapping("/checkout") public ResponseEntity<?> checkout(@Valid @RequestBody VentaRequest r){try{return ResponseEntity.status(201).body(service.crear(r,true));}catch(Exception e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}}
 @PutMapping("/{id}/estado") public ResponseEntity<?> estado(@PathVariable Long id,@Valid @RequestBody EstadoVentaRequest r){try{return ResponseEntity.ok(service.cambiarEstado(id,r.estado()));}catch(Exception e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}}
}
