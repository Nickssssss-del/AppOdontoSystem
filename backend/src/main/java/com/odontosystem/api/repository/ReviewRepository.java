package com.odontosystem.api.repository;

import com.odontosystem.api.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByDentistIdOrderByCreatedAtDesc(UUID dentistId);

    boolean existsByAppointmentId(UUID appointmentId);
}
