package com.odontosystem.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odontosystem.api.dto.AuthDtos.AuthRequest;
import com.odontosystem.api.dto.AuthDtos.AuthResponse;
import com.odontosystem.api.dto.AuthDtos.GoogleAuthRequest;
import com.odontosystem.api.dto.AuthDtos.RegisterRequest;
import com.odontosystem.api.dto.AuthDtos.UserDto;
import com.odontosystem.api.entity.RefreshToken;
import com.odontosystem.api.entity.User;
import com.odontosystem.api.entity.UserRole;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.RefreshTokenRepository;
import com.odontosystem.api.repository.UserRepository;
import com.odontosystem.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Lógica de negocio del módulo de autenticación.
 * Endpoints:
 *  - POST /api/v1/auth/login    (email + password)
 *  - POST /api/v1/auth/register (email + password)
 *  - POST /api/v1/auth/refresh
 *  - POST /api/v1/auth/google   (Google Sign-In)
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
    private static final String GOOGLE_TOKENINFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /** Client ID de la app Android/iOS en Google Cloud Console. Configúralo en .env. */
    @Value("${google.client-id:}")
    private String googleClientId;

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
                .role(UserRole.PATIENT)
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

    /**
     * Login/registro automático con Google. El cliente Android obtiene
     * un idToken firmado por Google (vía Credential Manager / Google
     * Sign-In SDK) y lo envía aquí. El backend lo valida directamente
     * contra el servidor de Google antes de confiar en él — nunca se
     * confía en un idToken sin verificar su firma y su "audience".
     *
     * Si es la primera vez que este correo inicia sesión, se crea la
     * cuenta automáticamente (con una contraseña aleatoria que el
     * usuario nunca usará, ya que siempre entrará por Google).
     */
    @Transactional
    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        GoogleUserInfo googleUser = verifyGoogleIdToken(request.getIdToken());

        User user = userRepository.findByEmail(googleUser.email())
                .orElseGet(() -> {
                    User created = User.builder()
                            .name(googleUser.name())
                            .email(googleUser.email())
                            .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .role(UserRole.PATIENT)
                            .build();
                    return userRepository.save(created);
                });

        return buildAuthResponse(user);
    }

    private GoogleUserInfo verifyGoogleIdToken(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_CLIENT_ID no está configurado en el backend (.env)");
        }

        JsonNode payload;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_TOKENINFO_URL + idToken))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw ApiException.unauthorized("Token de Google inválido o expirado");
            }

            payload = objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ApiException.badRequest("No se pudo validar el token con Google. Intenta de nuevo.");
        }

        String audience = payload.path("aud").asText("");
        if (!googleClientId.equals(audience)) {
            throw ApiException.unauthorized("Este token de Google no pertenece a esta aplicación");
        }

        boolean emailVerified = payload.path("email_verified").asBoolean(false);
        if (!emailVerified) {
            throw ApiException.unauthorized("El correo de Google no está verificado");
        }

        String email = payload.path("email").asText(null);
        String name = payload.path("name").asText(email);

        if (email == null) {
            throw ApiException.unauthorized("Google no devolvió un correo válido");
        }

        return new GoogleUserInfo(email, name);
    }

    private record GoogleUserInfo(String email, String name) {}

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
