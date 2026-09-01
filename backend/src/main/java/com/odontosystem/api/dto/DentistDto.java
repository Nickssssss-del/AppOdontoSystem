package com.odontosystem.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Espejo exacto de `Dentist` en Dentist.kt */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DentistDto {
    private UUID id;
    private String name;
    private String specialty;
    private String district;
    private String address;
    private Float rating;
    private Integer reviewsCount;
    private BigDecimal price;
    private List<String> availableDays;
    private String phone;
    private String imageUrl;
    private String avatarInitials;
}
