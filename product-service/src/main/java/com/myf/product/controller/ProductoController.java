package com.myf.product.controller;

import com.myf.product.dto.ProductoRequest;
import com.myf.product.service.ProductoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Consulta pública y administración de productos y presentaciones.")
@OpenAPIDefinition(info = @Info(title = "MYF Perfumes - Productos", version = "1.0.0",
        description = "Catálogo de perfumes con presentaciones de botella y decant. Las operaciones de escritura requieren JWT."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Token JWT obtenido en el servicio de autenticación.")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar productos activos", description = "Devuelve los productos activos y sus presentaciones disponibles para el catálogo público.",
            responses = @ApiResponse(responseCode = "200", description = "Lista de productos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "[{\"id\":1,\"categoriaId\":1,\"categoria\":\"Perfumes\",\"marcaId\":1,\"marca\":\"MYF\",\"codigo\":\"MYF-001\",\"nombre\":\"Perfume de muestra\",\"descripcion\":\"Fragancia\",\"genero\":\"UNISEX\",\"imagenUrl\":null,\"estado\":\"ACTIVO\",\"tipo\":\"PERFUME\",\"volumenMl\":100,\"precio\":250,\"costo\":150,\"presentaciones\":[{\"id\":1,\"codigo\":\"MYF-001-B-100-1\",\"tipo\":\"BOTELLA\",\"volumenMl\":100,\"precio\":250,\"costo\":150,\"estado\":\"ACTIVO\"}]}]"))))
    public Object listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar producto", description = "Obtiene el detalle de un producto, incluidas sus presentaciones.")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.obtener(id));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un producto con una o más presentaciones activas.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Perfume con botella y decant",
                                    value = "{\"categoriaId\":1,\"marcaId\":1,\"codigo\":\"MYF-001\",\"nombre\":\"Perfume de muestra\",\"descripcion\":\"Fragancia\",\"tipo\":\"PERFUME\",\"volumenMl\":100,\"genero\":\"UNISEX\",\"precio\":250,\"costo\":150,\"imagenUrl\":null,\"presentaciones\":[{\"tipo\":\"BOTELLA\",\"volumenMl\":100,\"precio\":250,\"costo\":150,\"activo\":true},{\"tipo\":\"DECANT\",\"volumenMl\":10,\"precio\":35,\"costo\":15,\"activo\":true}]}"))),
            responses = @ApiResponse(responseCode = "201", description = "Producto creado"))
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoRequest r) {
        try {
            return ResponseEntity.status(201).body(service.crear(r));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos y las presentaciones activas del producto.",
            security = @SecurityRequirement(name = "bearerAuth"))
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
    @Operation(summary = "Desactivar producto", description = "Desactiva el producto y sus presentaciones sin eliminar su historial.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "204", description = "Producto desactivado"))
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
    @Operation(summary = "Subir imagen de producto", security = @SecurityRequirement(name = "bearerAuth"))
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