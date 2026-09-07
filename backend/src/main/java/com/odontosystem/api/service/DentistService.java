package com.odontosystem.api.service;

import com.odontosystem.api.dto.DentistDto;
import com.odontosystem.api.entity.Dentist;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.DentistRepository;
import com.odontosystem.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Lógica de negocio del módulo de odontólogos.
 * Corresponde al endpoint GET /api/v1/dentists, consumido por
 * SearchActivity / DentistListViewModel en el cliente Android,
 * el cual admite filtrado opcional por distrito.
 */
@Service
@RequiredArgsConstructor
public class DentistService {

    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DentistDto> findAll(String district) {
        List<Dentist> dentists = StringUtils.hasText(district)
                ? dentistRepository.findByDistrictIgnoreCase(district)
                : dentistRepository.findAll();

        return dentists.stream().map(this::toDto).toList();
    }

    /**
     * Perfil de odontólogo del usuario actualmente autenticado (rol
     * DENTIST). Requiere que el registro `dentists` ya esté vinculado
     * con este usuario vía `user_id` (ver V4__link_dentist_user.sql).
     */
    @Transactional(readOnly = true)
    public DentistDto findMyProfile(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        Dentist dentist = dentistRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound(
                        "Tu cuenta todavía no está vinculada a un perfil de odontólogo"));

        return toDto(dentist);
    }

    private DentistDto toDto(Dentist d) {
        return DentistDto.builder()
                .id(d.getId())
                .name(d.getName())
                .specialty(d.getSpecialty())
                .district(d.getDistrict())
                .address(d.getAddress())
                .rating(d.getRating())
                .reviewsCount(d.getReviewsCount())
                .price(d.getPrice())
                .availableDays(d.getAvailableDays())
                .phone(d.getPhone())
                .imageUrl(d.getImageUrl())
                .avatarInitials(d.getAvatarInitials())
                .build();
    }
}
