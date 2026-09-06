package com.odontosystem.api.dto;

import com.odontosystem.api.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

public class AuthDtos {

    /** Espejo exacto de `AuthRequest` en AuthModels.kt */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;

        private UserRole role = UserRole.PATIENT;
    }

    /** Espejo exacto de `User` en AuthModels.kt (sin exponer el hash de contraseña) */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        private UUID id;
        private String name;
        private String email;
        private UserRole role;
        private String phone;
    }

    /** Espejo exacto de `AuthResponse` en AuthModels.kt, ahora incluye refreshToken */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String token;
        private String refreshToken;
        private UserDto user;
    }

    /** Body de POST /api/v1/auth/register */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank
        private String name;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;

        private UserRole role = UserRole.PATIENT;
    }

    /** Body de POST /api/v1/auth/refresh */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;
    }
}