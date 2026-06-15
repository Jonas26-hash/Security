package com.transporte.cliente.controller;

import com.transporte.cliente.dto.LoginRequest;
import com.transporte.cliente.dto.LoginResponse;
import com.transporte.cliente.dto.RegisterRequest;
import com.transporte.cliente.dto.RegisterResponse;
import com.transporte.cliente.entity.User;
import com.transporte.cliente.service.AuthService;
import com.transporte.cliente.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints de autenticación y registro")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica un usuario y retorna un JWT")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.authenticate(request);
            String token = jwtService.generateToken(user);

            // Obtener el rol principal
            String role = user.getRoles().stream()
                    .findFirst()
                    .map(r -> r.getName().toUpperCase())
                    .orElse("CLIENTE");

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .expiresIn(jwtService.getExpirationTime() / 1000)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(role)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Registro de usuario", description = "Registra un nuevo usuario")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.registerUser(request);

            RegisterResponse response = RegisterResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .message("Usuario registrado exitosamente")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error en registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RegisterResponse.builder()
                            .message("Error: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida si un token JWT es válido")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(jwtService.validateToken(token));
    }
}
