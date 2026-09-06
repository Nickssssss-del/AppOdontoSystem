package com.odontosystem.api.repository;

import com.odontosystem.api.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByPatientIdOrderByDateDescTimeDesc(UUID patientId);

    Optional<Appointment> findByIdAndPatientId(UUID id, UUID patientId);
}