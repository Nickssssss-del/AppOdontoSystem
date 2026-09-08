package com.odontosystem.api.repository;

import com.odontosystem.api.entity.ClinicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, UUID> {
    Optional<ClinicalRecord> findByAppointmentId(UUID appointmentId);

    List<ClinicalRecord> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
