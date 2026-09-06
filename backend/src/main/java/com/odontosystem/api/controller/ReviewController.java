package com.odontosystem.api.controller;

import com.odontosystem.api.dto.ReviewDtos.CreateReviewRequest;
import com.odontosystem.api.dto.ReviewDtos.ReviewDto;
import com.odontosystem.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Rutas protegidas (requieren JWT):
 *  - POST /api/v1/reviews
 *  - GET  /api/v1/dentists/{dentistId}/reviews
 * Corresponden a RF-07 (Calificación y Reseñas de Atención).
 */
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/v1/reviews")
    public ResponseEntity<ReviewDto> create(
            Authentication authentication,
            @Valid @RequestBody CreateReviewRequest request) {
        String email = authentication.getName();
        ReviewDto created = reviewService.create(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/v1/dentists/{dentistId}/reviews")
    public ResponseEntity<List<ReviewDto>> byDentist(@PathVariable UUID dentistId) {
        return ResponseEntity.ok(reviewService.findByDentist(dentistId));
    }
}
