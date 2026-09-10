package com.odontosystem.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de usuario (paciente u odontólogo con acceso a la app).
 * Corresponde a la tabla `users` (ver V1__init_schema.sql).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Hash BCrypt de la contraseña. Nunca se expone en las respuestas de la API.
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.PATIENT;

    @Column(length = 20)
    @Builder.Default
    private String phone = "+51987654321";

    @Column(name = "last_name", length = 150)
    private String lastName;

    @Column(name = "cop_number", length = 50)
    private String copNumber;

    @Column(name = "documents", columnDefinition = "text[]")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.ARRAY)
    private String[] documents;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
