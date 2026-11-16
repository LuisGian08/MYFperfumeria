package com.myf.auth.controller;

import com.myf.auth.dto.*;
import com.myf.auth.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y emisión de tokens JWT.")
@OpenAPIDefinition(info = @Info(title = "MYF Perfumes - Autenticación", version = "1.0.0",
        description = "Servicio de autenticación de MYF Perfumes. El token emitido se usa como Bearer en las operaciones protegidas."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Pega el token JWT emitido por /api/auth/login. Swagger agrega el prefijo Bearer.")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida las credenciales y devuelve un JWT para las solicitudes protegidas.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true, content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "Credenciales", value = "{\"email\":\"admin@myf.local\",\"password\":\"Admin123*\"}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autenticación correcta",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class),
                                    examples = @ExampleObject(value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9...\",\"userId\":1,\"email\":\"admin@myf.local\",\"nombres\":\"Administrador\"}"))),
                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"error\":\"Credenciales inválidas\"}")))
            })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest r){
        try{return ResponseEntity.ok(service.login(r));}
        catch(IllegalArgumentException e){return ResponseEntity.status(401).body(Map.of("error",e.getMessage()));}
    }
}
