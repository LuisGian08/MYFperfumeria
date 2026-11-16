package com.myf.report.controller;
import com.myf.report.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
@RestController @RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Indicadores agregados de ventas y productos vendidos.")
@OpenAPIDefinition(info = @Info(title = "MYF Perfumes - Reportes", version = "1.0.0",
        description = "Consultas de reportes alimentados asincrónicamente por eventos de ventas."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Token JWT obtenido en el servicio de autenticación.")
public class ReporteController {
 private final ReporteVentaRepository ventas; private final ReporteProductoRepository productos;
 public ReporteController(ReporteVentaRepository v,ReporteProductoRepository p){ventas=v;productos=p;}
 @GetMapping("/ventas")
 @Operation(summary = "Consultar ventas agregadas", description = "Devuelve el resumen diario de ventas procesadas.", security = @SecurityRequirement(name = "bearerAuth"))
 public Object ventas(){return ventas.findAll();}
 @GetMapping("/productos")
 @Operation(summary = "Consultar productos más vendidos", description = "Sin fecha devuelve el top general; con fecha, los productos vendidos ese día.", security = @SecurityRequirement(name = "bearerAuth"))
 public Object productos(@RequestParam(required=false) LocalDate fecha){return fecha==null?productos.findTop10ByOrderByCantidadVendidaDesc():productos.findByFechaOrderByCantidadVendidaDesc(fecha);}
 @GetMapping("/dashboard")
 @Operation(summary = "Consultar indicadores del día", security = @SecurityRequirement(name = "bearerAuth"))
 public Object dashboard(){var today=LocalDate.now();var d=ventas.findByFecha(today).orElse(null);Map<String,Object> m=new LinkedHashMap<>();m.put("fecha",today);m.put("ventasHoy",d==null?0:d.getCantidadVentas());m.put("unidadesHoy",d==null?0:d.getUnidadesVendidas());m.put("botellasHoy",d==null?0:d.getBotellasVendidas());m.put("decantsHoy",d==null?0:d.getDecantsVendidos());m.put("ingresosHoy",d==null?0:d.getTotalVentas());m.put("topProductos",productos.findByFechaOrderByCantidadVendidaDesc(today).stream().limit(5).toList());return m;}
}
