package com.myf.sales.controller;

import com.myf.sales.dto.*;
import com.myf.sales.service.VentaService;
import jakarta.validation.Valid;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

 private final VentaService service;

 public VentaController(VentaService service) {
  this.service = service;
 }

 // ======================================================
 // LISTAR VENTAS
 // ======================================================

 @GetMapping
 public Object listar() {
  return service.listar();
 }

 // ======================================================
 // OBTENER VENTA
 // ======================================================

 @GetMapping("/{id}")
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