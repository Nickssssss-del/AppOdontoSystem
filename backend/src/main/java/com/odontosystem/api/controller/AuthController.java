package com.odontosystem.api.controller;

import com.odontosystem.api.dto.AuthDtos.AuthRequest;
import com.odontosystem.api.dto.AuthDtos.AuthResponse;
import com.odontosystem.api.dto.AuthDtos.GoogleAuthRequest;
import com.odontosystem.api.dto.AuthDtos.RefreshRequest;
import com.odontosystem.api.dto.AuthDtos.RegisterRequest;
import com.odontosystem.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Rutas públicas (ver SecurityConfig):
 *  - POST /api/v1/auth/login
 *  - POST /api/v1/auth/register
 *  - POST /api/v1/auth/refresh
 *  - POST /api/v1/auth/google
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }
}
