package com.myf.inventory.controller;

import com.myf.inventory.dto.*;
import com.myf.inventory.service.InventarioService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Consulta de disponibilidad y operaciones de stock de botellas y decants.")
@OpenAPIDefinition(info = @Info(title = "MYF Perfumes - Inventario", version = "1.0.0",
        description = "Gestión de unidades cerradas, volumen para decants y movimientos de inventario."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Token JWT obtenido en el servicio de autenticación.")
public class InventarioController {
    private final InventarioService service;
    public InventarioController(InventarioService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Consultar inventario", description = "Devuelve las existencias detalladas para administración.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object listar() { return service.listar(); }

    @GetMapping("/movimientos")
    @Operation(summary = "Consultar movimientos de inventario", description = "Lista los últimos movimientos o filtra por producto.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object movimientos(@RequestParam(required = false) Long productoId) {
        return productoId == null ? service.movimientos() : service.movimientos(productoId);
    }

    @GetMapping("/{productoId}")
    @Operation(summary = "Consultar stock de un producto", description = "Devuelve unidades y mililitros disponibles para decants.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> get(@PathVariable Long productoId) {
        try { return ResponseEntity.ok(service.obtener(productoId)); }
        catch (Exception e) { return ResponseEntity.status(404).body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/configurar")
    @Operation(summary = "Configurar stock", description = "Establece cantidades actuales y umbrales de alerta.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"stockActual\":5,\"stockMinimo\":2,\"volumenDecantMl\":120,\"volumenDecantMinimoMl\":20}"))))
    public ResponseEntity<?> configurar(@PathVariable Long productoId, @Valid @RequestBody StockConfigRequest r) {
        try { return ResponseEntity.ok(service.configurar(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/entrada")
    @Operation(summary = "Registrar entrada de botellas", security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"cantidad\":5,\"motivo\":\"Reposición\",\"usuarioId\":1,\"referenciaVentaId\":null}"))))
    public ResponseEntity<?> entrada(@PathVariable Long productoId, @Valid @RequestBody MovimientoRequest r) {
        try { return ResponseEntity.ok(service.entrada(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/salida")
    @Operation(summary = "Registrar salida de botellas", description = "Descuenta unidades disponibles; falla si no hay stock suficiente.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> salida(@PathVariable Long productoId, @Valid @RequestBody MovimientoRequest r) {
        try { return ResponseEntity.ok(service.salida(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/decant/entrada")
    @Operation(summary = "Registrar entrada de volumen para decants", security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"mililitros\":100,\"motivo\":\"Ajuste de volumen\",\"usuarioId\":1,\"referenciaVentaId\":null}"))))
    public ResponseEntity<?> entradaDecant(@PathVariable Long productoId, @Valid @RequestBody MovimientoDecantRequest r) {
        try { return ResponseEntity.ok(service.entradaDecant(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/decant/salida")
    @Operation(summary = "Registrar salida de volumen para decants", description = "Descuenta mililitros disponibles; falla si el volumen es insuficiente.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> salidaDecant(@PathVariable Long productoId, @Valid @RequestBody MovimientoDecantRequest r) {
        try { return ResponseEntity.ok(service.salidaDecant(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/{productoId}/decant/abrir")
    @Operation(summary = "Abrir botella para decants", description = "Descuenta una botella cerrada y agrega sus mililitros al inventario de decants.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"mililitros\":100,\"motivo\":\"Botella abierta para decants\",\"usuarioId\":1}"))))
    public ResponseEntity<?> abrirBotellaParaDecants(@PathVariable Long productoId, @Valid @RequestBody AbrirBotellaRequest r) {
        try { return ResponseEntity.ok(service.abrirBotellaParaDecants(productoId, r)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}
