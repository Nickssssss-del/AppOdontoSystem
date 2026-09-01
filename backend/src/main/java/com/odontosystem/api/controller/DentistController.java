package com.odontosystem.api.controller;

import com.odontosystem.api.dto.DentistDto;
import com.odontosystem.api.service.DentistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GET /api/v1/dentists?district=Miraflores
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
}
