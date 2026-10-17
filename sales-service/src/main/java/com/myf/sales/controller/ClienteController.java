package com.myf.sales.controller;
import com.myf.sales.service.VentaService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/clientes")
public class ClienteController { private final VentaService service; public ClienteController(VentaService service){this.service=service;} @GetMapping public Object listar(){return service.listarClientes();} }
