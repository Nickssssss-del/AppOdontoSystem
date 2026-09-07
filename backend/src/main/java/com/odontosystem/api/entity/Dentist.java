package com.odontosystem.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Entidad Odontólogo. Corresponde 1:1 con el modelo `Dentist.kt` del
 * cliente Android para que el mapeo JSON <-> Kotlin sea directo.
 */
@Entity
@Table(name = "dentists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dentist {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 120)
    private String specialty;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(length = 255)
    private String address;

    @Builder.Default
    private Float rating = 0f;

    @Column(name = "reviews_count")
    @Builder.Default
    private Integer reviewsCount = 0;

    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    // Mapea directamente a un arreglo TEXT[] de PostgreSQL (Ej: {"Lunes","Miércoles"})
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "available_days")
    @Builder.Default
    private List<String> availableDays = List.of();

    @Builder.Default
    private String phone = "51987654321";

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "avatar_initials", length = 5)
    @Builder.Default
    private String avatarInitials = "DR";

    /**
     * Cuenta de usuario (rol DENTIST) vinculada a este perfil. Es
     * opcional: los odontólogos sembrados en V1 no tienen cuenta real
     * hasta que se vinculen manualmente o mediante un flujo de
     * "reclamar perfil" (ver V4__link_dentist_user.sql).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
