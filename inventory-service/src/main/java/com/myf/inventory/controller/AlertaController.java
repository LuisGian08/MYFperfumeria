package com.myf.inventory.controller;

import com.myf.inventory.entity.AlertaStock;
import com.myf.inventory.repository.AlertaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "Alertas", description = "Consulta y atención de alertas de inventario.")
@OpenAPIDefinition(info = @Info(
        title = "MYF Perfumes - Alertas",
        version = "1.0.0",
        description = "Alertas generadas a partir de eventos de inventario."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Token JWT obtenido en el servicio de autenticación.")
public class AlertaController {
    private final AlertaRepository repository;

    public AlertaController(AlertaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Listar alertas pendientes",
            security = @SecurityRequirement(name = "bearerAuth"))
    public List<AlertaStock> pendientes() {
        return repository.findByEstadoOrderByFechaAlertaDesc("PENDIENTE");
    }

    @PutMapping("/{id}/atender")
    @Operation(summary = "Marcar alerta como atendida",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> atender(@PathVariable Long id) {
        return repository.findById(id).<ResponseEntity<?>>map(alert -> {
            alert.setEstado("ATENDIDA");
            alert.setFechaAtendida(LocalDateTime.now());
            return ResponseEntity.ok(repository.save(alert));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
