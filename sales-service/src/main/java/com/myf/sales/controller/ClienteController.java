package com.myf.sales.controller;
import com.myf.sales.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Consulta de clientes registrados en las ventas.")
public class ClienteController {
 private final VentaService service;
 public ClienteController(VentaService service){this.service=service;}
 @GetMapping
 @Operation(summary = "Listar clientes", security = @SecurityRequirement(name = "bearerAuth"))
 public Object listar(){return service.listarClientes();}
}
