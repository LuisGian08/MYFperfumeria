package com.myf.sales.controller;

import com.myf.sales.entity.ReporteVentaDiaria;
import com.myf.sales.repository.ReporteProductoRepository;
import com.myf.sales.repository.ReporteVentaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Indicadores agregados de ventas y productos vendidos.")
@OpenAPIDefinition(info = @Info(
        title = "MYF Perfumes - Reportes",
        version = "1.0.0",
        description = "Consultas de reportes alimentados asincrónicamente por eventos de ventas."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Token JWT obtenido en el servicio de autenticación.")
public class ReporteController {
    private final ReporteVentaRepository ventas;
    private final ReporteProductoRepository productos;

    public ReporteController(ReporteVentaRepository ventas, ReporteProductoRepository productos) {
        this.ventas = ventas;
        this.productos = productos;
    }

    @GetMapping("/ventas")
    @Operation(summary = "Consultar ventas agregadas",
            description = "Devuelve el resumen diario de ventas procesadas.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object ventas() {
        return ventas.findAll();
    }

    @GetMapping("/productos")
    @Operation(summary = "Consultar productos más vendidos",
            description = "Sin fecha devuelve el top general; con fecha, los productos vendidos ese día.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object productos(@RequestParam(required = false) LocalDate fecha) {
        return fecha == null
                ? productos.findTop10ByOrderByCantidadVendidaDesc()
                : productos.findByFechaOrderByCantidadVendidaDesc(fecha);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Consultar indicadores del día",
            security = @SecurityRequirement(name = "bearerAuth"))
    public Object dashboard() {
        LocalDate today = LocalDate.now();
        ReporteVentaDiaria daily = ventas.findByFecha(today).orElse(null);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("fecha", today);
        response.put("ventasHoy", daily == null ? 0 : daily.getCantidadVentas());
        response.put("unidadesHoy", daily == null ? 0 : daily.getUnidadesVendidas());
        response.put("botellasHoy", daily == null ? 0 : daily.getBotellasVendidas());
        response.put("decantsHoy", daily == null ? 0 : daily.getDecantsVendidos());
        response.put("ingresosHoy", daily == null ? 0 : daily.getTotalVentas());
        response.put("topProductos", productos.findByFechaOrderByCantidadVendidaDesc(today)
                .stream()
                .limit(5)
                .toList());
        return response;
    }
}
