package com.myf.report.controller;
import com.myf.report.repository.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
@RestController @RequestMapping("/api/reportes")
public class ReporteController {
 private final ReporteVentaRepository ventas; private final ReporteProductoRepository productos;
 public ReporteController(ReporteVentaRepository v,ReporteProductoRepository p){ventas=v;productos=p;}
 @GetMapping("/ventas") public Object ventas(){return ventas.findAll();}
 @GetMapping("/productos") public Object productos(@RequestParam(required=false) LocalDate fecha){return fecha==null?productos.findTop10ByOrderByCantidadVendidaDesc():productos.findByFechaOrderByCantidadVendidaDesc(fecha);}
 @GetMapping("/dashboard") public Object dashboard(){var today=LocalDate.now();var d=ventas.findByFecha(today).orElse(null);Map<String,Object> m=new LinkedHashMap<>();m.put("fecha",today);m.put("ventasHoy",d==null?0:d.getCantidadVentas());m.put("unidadesHoy",d==null?0:d.getUnidadesVendidas());m.put("botellasHoy",d==null?0:d.getBotellasVendidas());m.put("decantsHoy",d==null?0:d.getDecantsVendidos());m.put("ingresosHoy",d==null?0:d.getTotalVentas());m.put("topProductos",productos.findByFechaOrderByCantidadVendidaDesc(today).stream().limit(5).toList());return m;}
}
