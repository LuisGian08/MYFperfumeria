package com.myf.auth.controller;

import com.myf.auth.dto.*;
import com.myf.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest r){
        try{return ResponseEntity.ok(service.login(r));}
        catch(IllegalArgumentException e){return ResponseEntity.status(401).body(Map.of("error",e.getMessage()));}
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r){
        try{
            var u = service.register(r);
            return ResponseEntity.status(201).body(Map.of(
                    "mensaje", "Usuario registrado exitosamente",
                    "userId", u.userId(),
                    "email", u.email(),
                    "fechaHora", java.time.LocalDateTime.now().toString()));
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
