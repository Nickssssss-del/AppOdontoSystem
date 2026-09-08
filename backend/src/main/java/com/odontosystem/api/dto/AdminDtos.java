package com.odontosystem.api.dto;

import com.odontosystem.api.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class AdminDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {
        @NotBlank private String name;
        @NotBlank @Email private String email;
        @NotBlank private String password;
        @NotNull private UserRole role;
        private String phone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        @NotBlank private String name;
        @NotNull private UserRole role;
        private String phone;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserAdminDto {
        private UUID id;
        private String name;
        private String email;
        private UserRole role;
        private String phone;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AuditDto {
        private UUID id;
        private String actorEmail;
        private String action;
        private String resource;
        private UUID resourceId;
        private LocalDateTime createdAt;
    }
}