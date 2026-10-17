package com.myf.product.controller;

import com.myf.product.dto.ProductoRequest;
import com.myf.product.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public Object listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.obtener(id));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoRequest r) {
        try {
            return ResponseEntity.status(201).body(service.crear(r));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest r) {

        try {
            return ResponseEntity.ok(service.actualizar(id, r));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrar(@PathVariable Long id) {
        try {
            service.desactivar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==========================
    // SUBIR IMAGEN DE PRODUCTO
    // ==========================

    @PostMapping(
            value = "/imagen",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> subirImagen(
            @RequestParam("imagen") MultipartFile imagen) {

        try {

            if (imagen.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Debes seleccionar una imagen"));
            }

            String contentType = imagen.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El archivo debe ser una imagen"));
            }

            String original = imagen.getOriginalFilename();
            String extension = "";

            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf("."));
            }

            String nombreArchivo =
                    UUID.randomUUID().toString() + extension;

            Path carpeta = Paths.get("uploads", "productos");

            Files.createDirectories(carpeta);

            Path destino = carpeta.resolve(nombreArchivo);

            Files.copy(
                    imagen.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String url = "/imagenes/productos/" + nombreArchivo;

            return ResponseEntity.ok(
                    Map.of(
                            "url", url,
                            "mensaje", "Imagen subida correctamente"
                    )
            );

        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "No se pudo guardar la imagen"
                    ));
        }
    }
}