package com.myf.alert.controller;

import com.myf.alert.entity.AlertaStock;
import com.myf.alert.repository.AlertaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Consulta y atención de alertas de inventario.")
@OpenAPIDefinition(info = @Info(title = "MYF Perfumes - Alertas", version = "1.0.0",
        description = "Alertas generadas a partir de eventos de inventario."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Token JWT obtenido en el servicio de autenticación.")
public class AlertaController {
    private final AlertaRepository r;
    public AlertaController(AlertaRepository r) { this.r = r; }

    @GetMapping
    @Operation(summary = "Listar alertas pendientes", security = @SecurityRequirement(name = "bearerAuth"))
    public List<AlertaStock> pendientes() {
        return r.findByEstadoOrderByFechaAlertaDesc("PENDIENTE");
    }

    @PutMapping("/{id}/atender")
    @Operation(summary = "Marcar alerta como atendida", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> atender(@PathVariable Long id) {
        return r.findById(id).<ResponseEntity<?>>map(a -> {
            a.setEstado("ATENDIDA");
            a.setFechaAtendida(LocalDateTime.now());
            return ResponseEntity.ok(r.save(a));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
