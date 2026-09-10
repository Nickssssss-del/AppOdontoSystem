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
        private String lastName;
        private String email;
        private UserRole role;
        private String phone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank
        private String name;

        @NotBlank
        private String lastName;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String phone;

        @NotBlank
        private String password;

        private UserRole role = UserRole.PATIENT;

        private String copNumber;

        private java.util.List<String> documents;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleAuthRequest {
        @NotBlank
        private String idToken;

        private UserRole role = UserRole.PATIENT;
    }

    /** Espejo exacto de `AuthResponse` en AuthModels.kt */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String token;
        private UserDto user;
    }
}
