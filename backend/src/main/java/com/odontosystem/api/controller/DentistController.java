package com.odontosystem.api.controller;

import com.odontosystem.api.dto.DentistDto;
import com.odontosystem.api.service.DentistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GET /api/v1/dentists?district=Miraflores  (público dentro de la app, requiere JWT)
 * GET /api/v1/dentists/me                    (perfil propio, solo rol DENTIST vinculado)
 * Ruta protegida (requiere JWT). Consumida por SearchActivity en el cliente Android.
 */
@RestController
@RequestMapping("/api/v1/dentists")
@RequiredArgsConstructor
public class DentistController {

    private final DentistService dentistService;

    @GetMapping
    public ResponseEntity<List<DentistDto>> getDentists(
            @RequestParam(required = false) String district) {
        return ResponseEntity.ok(dentistService.findAll(district));
    }

    @GetMapping("/me")
    public ResponseEntity<DentistDto> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(dentistService.findMyProfile(email));
    }
}
