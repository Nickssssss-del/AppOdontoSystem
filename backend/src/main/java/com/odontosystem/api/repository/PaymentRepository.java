package com.odontosystem.api.repository;

import com.odontosystem.api.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByAppointmentId(UUID appointmentId);

    boolean existsByAppointmentId(UUID appointmentId);
}
