package com.myf.sales.controller;

import com.myf.sales.dto.*;
import com.myf.sales.service.VentaService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas y pedidos", description = "Registro de ventas, checkout público y gestión de estados.")
@OpenAPIDefinition(info = @Info(title = "MYF Perfumes - Ventas", version = "1.0.0",
        description = "Ventas recalculan precios desde el catálogo y validan el inventario antes de registrar."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Token JWT obtenido en el servicio de autenticación.")
public class VentaController {

 private final VentaService service;

 public VentaController(VentaService service) {
  this.service = service;
 }

 // ======================================================
 // LISTAR VENTAS
 // ======================================================

 @GetMapping
 @Operation(summary = "Listar ventas", description = "Lista las ventas ordenadas desde la más reciente.", security = @SecurityRequirement(name = "bearerAuth"))
 public Object listar() {
  return service.listar();
 }

 // ======================================================
 // OBTENER VENTA
 // ======================================================

 @GetMapping("/{id}")
 @Operation(summary = "Consultar venta", security = @SecurityRequirement(name = "bearerAuth"))
 public ResponseEntity<?> get(@PathVariable Long id) {

  try {

   return ResponseEntity.ok(
           service.obtener(id)
   );

  } catch (Exception e) {

   return ResponseEntity.status(404)
           .body(
                   Map.of(
                           "error",
                           e.getMessage()
                   )
           );
  }
 }

 // ======================================================
 // CREAR VENTA DESDE ADMIN
 // ======================================================

 @PostMapping
 @Operation(summary = "Registrar venta administrativa", description = "Registra una venta de administración y descuenta stock. El descuento se valida en el servidor.",
         security = @SecurityRequirement(name = "bearerAuth"),
         requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                 content = @Content(mediaType = "application/json",
                         examples = @ExampleObject(name = "Venta administrativa",
                                 value = "{\"usuarioId\":1,\"descuento\":0,\"metodoPago\":\"YAPE\",\"observacion\":\"Venta en tienda\",\"canal\":\"ADMIN\",\"cliente\":null,\"items\":[{\"productoId\":1,\"presentacionId\":1,\"cantidad\":1}]}"))))
 public ResponseEntity<?> crear(
         @Valid @RequestBody VentaRequest r) {

  try {

   return ResponseEntity.status(201)
           .body(
                   service.crear(r, false)
           );

  } catch (Exception e) {

   return ResponseEntity.badRequest()
           .body(
                   Map.of(
                           "error",
                           e.getMessage()
                   )
           );
  }
 }

 // ======================================================
 // CHECKOUT WEB
 // ======================================================

 @PostMapping("/checkout")
 @Operation(summary = "Crear pedido desde la tienda", description = "Endpoint público. Recalcula precios y verifica stock en el backend; no acepta descuentos del cliente.",
         requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                 content = @Content(mediaType = "application/json",
                         examples = @ExampleObject(name = "Checkout público",
                                 value = "{\"descuento\":0,\"metodoPago\":\"YAPE\",\"observacion\":\"Entregar por la tarde\",\"canal\":\"WEB\",\"cliente\":{\"nombre\":\"Ana Pérez\",\"telefono\":\"999123456\",\"email\":\"ana@example.com\",\"direccion\":\"Lima\"},\"items\":[{\"productoId\":1,\"presentacionId\":2,\"cantidad\":2}]}"))))
 public ResponseEntity<?> checkout(
         @Valid @RequestBody VentaRequest r) {

  try {

   return ResponseEntity.status(201)
           .body(
                   service.crear(r, true)
           );

  } catch (Exception e) {

   return ResponseEntity.badRequest()
           .body(
                   Map.of(
                           "error",
                           e.getMessage()
                   )
           );
  }
 }

 // ======================================================
 // CAMBIAR ESTADO DEL PEDIDO
 // ======================================================

 @PutMapping("/{id}/estado")
 @Operation(summary = "Cambiar estado de pedido", description = "Actualiza el estado. Un pedido cancelado no se reactiva automáticamente.",
         security = @SecurityRequirement(name = "bearerAuth"),
         requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                 content = @Content(mediaType = "application/json",
                         examples = @ExampleObject(value = "{\"estado\":\"CONFIRMADO\"}"))))
 public ResponseEntity<?> estado(
         @PathVariable Long id,
         @Valid @RequestBody EstadoVentaRequest r) {

  try {

   return ResponseEntity.ok(
           service.cambiarEstado(
                   id,
                   r.estado()
           )
   );

  } catch (Exception e) {

   return ResponseEntity.badRequest()
           .body(
                   Map.of(
                           "error",
                           e.getMessage()
                   )
           );
  }
 }

 // ======================================================
 // CAMBIAR ESTADO DEL PAGO
 // ======================================================

 @PutMapping("/{id}/pago")
 @Operation(summary = "Cambiar estado del pago", security = @SecurityRequirement(name = "bearerAuth"))
 public ResponseEntity<?> estadoPago(
         @PathVariable Long id,
         @RequestBody Map<String, String> body) {

  try {

   String estadoPago =
           body.get("estadoPago");

   if (
           estadoPago == null ||
                   estadoPago.isBlank()
   ) {

    return ResponseEntity.badRequest()
            .body(
                    Map.of(
                            "error",
                            "El estado de pago es obligatorio"
                    )
            );
   }

   return ResponseEntity.ok(
           service.cambiarEstadoPago(
                   id,
                   estadoPago
           )
   );

  } catch (Exception e) {

   return ResponseEntity.badRequest()
           .body(
                   Map.of(
                           "error",
                           e.getMessage()
                   )
           );
  }
 }

 // ======================================================
 // SUBIR COMPROBANTE DE PAGO
 // ======================================================

 @PostMapping(
         value = "/{id}/comprobante",
         consumes = MediaType.MULTIPART_FORM_DATA_VALUE
 )
 @Operation(summary = "Subir comprobante de pago")
 public ResponseEntity<?> subirComprobante(
         @PathVariable Long id,
         @RequestParam("comprobante")
         MultipartFile comprobante) {

  try {

   // Verificar que exista el pedido
   service.obtener(id);

   if (comprobante.isEmpty()) {

    return ResponseEntity.badRequest()
            .body(
                    Map.of(
                            "error",
                            "Debes seleccionar un comprobante"
                    )
            );
   }

   // Validar que sea imagen
   String contentType =
           comprobante.getContentType();

   if (
           contentType == null ||
                   !contentType.startsWith("image/")
   ) {

    return ResponseEntity.badRequest()
            .body(
                    Map.of(
                            "error",
                            "El comprobante debe ser una imagen"
                    )
            );
   }

   // Máximo 5 MB
   if (
           comprobante.getSize() >
                   5 * 1024 * 1024
   ) {

    return ResponseEntity.badRequest()
            .body(
                    Map.of(
                            "error",
                            "La imagen no puede superar los 5 MB"
                    )
            );
   }

   String original =
           comprobante.getOriginalFilename();

   String extension = ".jpg";

   if (
           original != null &&
                   original.contains(".")
   ) {

    extension =
            original.substring(
                    original.lastIndexOf(".")
            );
   }

   String nombreArchivo =
           "comprobante-" +
                   id +
                   "-" +
                   UUID.randomUUID() +
                   extension;

   Path carpeta =
           Paths.get(
                   "uploads",
                   "comprobantes"
           );

   Files.createDirectories(carpeta);

   Path destino =
           carpeta.resolve(
                   nombreArchivo
           );

   Files.copy(
           comprobante.getInputStream(),
           destino,
           StandardCopyOption.REPLACE_EXISTING
   );

   String url =
           "/comprobantes/" +
                   nombreArchivo;

   // Guardar URL en la venta
   service.guardarComprobante(
           id,
           url
   );

   return ResponseEntity.ok(
           Map.of(
                   "mensaje",
                   "Comprobante subido correctamente",
                   "url",
                   url
           )
   );

  } catch (IOException e) {

   return ResponseEntity
           .status(
                   HttpStatus.INTERNAL_SERVER_ERROR
           )
           .body(
                   Map.of(
                           "error",
                           "No se pudo guardar el comprobante"
                   )
           );

  } catch (Exception e) {

   return ResponseEntity.badRequest()
           .body(
                   Map.of(
                           "error",
                           e.getMessage()
                   )
           );
  }
 }
}