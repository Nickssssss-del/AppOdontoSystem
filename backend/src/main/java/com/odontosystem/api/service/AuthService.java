package com.odontosystem.api.service;

import com.odontosystem.api.dto.AuthDtos.AuthRequest;
import com.odontosystem.api.dto.AuthDtos.AuthResponse;
import com.odontosystem.api.dto.AuthDtos.UserDto;
import com.odontosystem.api.entity.User;
import com.odontosystem.api.entity.UserRole;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.UserRepository;
import com.odontosystem.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio del módulo de autenticación.
 * Corresponde al endpoint POST /api/v1/auth/login consumido por
 * LoginActivity / LoginViewModel en el cliente Android.
 *
 * Nota: se incluye también un método de registro (no mapeado aún a
 * un endpoint en el cliente actual) por si se requiere una pantalla
 * de registro de pacientes en una futura iteración del sprint.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId().toString(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .user(toDto(user))
                .build();
    }

    @Transactional
    public AuthResponse register(String name, String email, String rawPassword, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Ya existe una cuenta registrada con ese correo");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getId().toString(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .user(toDto(user))
                .build();
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .build();
    }
}
