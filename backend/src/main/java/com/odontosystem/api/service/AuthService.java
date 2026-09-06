package com.odontosystem.api.service;

import com.odontosystem.api.dto.AuthDtos.AuthRequest;
import com.odontosystem.api.dto.AuthDtos.AuthResponse;
import com.odontosystem.api.dto.AuthDtos.RegisterRequest;
import com.odontosystem.api.dto.AuthDtos.UserDto;
import com.odontosystem.api.entity.RefreshToken;
import com.odontosystem.api.entity.User;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.RefreshTokenRepository;
import com.odontosystem.api.repository.UserRepository;
import com.odontosystem.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Lógica de negocio del módulo de autenticación.
 * Endpoints:
 *  - POST /api/v1/auth/login    (LoginActivity / LoginViewModel)
 *  - POST /api/v1/auth/register
 *  - POST /api/v1/auth/refresh
 *
 * El refresh token es un valor opaco (UUID aleatorio) que el cliente
 * guarda en almacenamiento cifrado (RNF-02). En la base solo se
 * persiste su hash SHA-256 (tabla refresh_tokens, ver V2), nunca el
 * valor en texto plano, igual que se hace con las contraseñas.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long REFRESH_TOKEN_DAYS = 30;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Credenciales inválidas");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Ya existe una cuenta registrada con ese correo");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /**
     * Intercambia un refresh token válido (no expirado, no revocado) por
     * un nuevo access token JWT. Rota el refresh token: el anterior se
     * revoca y se emite uno nuevo, para limitar el daño si el valor
     * anterior llegó a filtrarse.
     */
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String incomingHash = hash(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(incomingHash)
                .orElseThrow(() -> ApiException.unauthorized("Refresh token inválido"));

        if (stored.isRevoked()) {
            throw ApiException.unauthorized("Refresh token revocado");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw ApiException.unauthorized("Refresh token expirado, inicia sesión de nuevo");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(stored.getUser());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user.getEmail(), user.getId().toString(), user.getRole().name());
        String rawRefreshToken = issueRefreshToken(user);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(rawRefreshToken)
                .user(toDto(user))
                .build();
    }

    private String issueRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
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