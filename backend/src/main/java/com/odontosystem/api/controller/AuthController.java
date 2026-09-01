package com.odontosystem.api.controller;

import com.odontosystem.api.dto.AuthDtos.AuthRequest;
import com.odontosystem.api.dto.AuthDtos.AuthResponse;
import com.odontosystem.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * POST /api/v1/auth/login
 * Ruta pública (ver SecurityConfig). Consumida por LoginActivity en el cliente Android.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
