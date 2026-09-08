package com.odontosystem.api.repository;

import com.odontosystem.api.entity.OdontogramTooth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OdontogramToothRepository extends JpaRepository<OdontogramTooth, UUID> {
    List<OdontogramTooth> findByPatientIdOrderByToothNumberAsc(UUID patientId);

    Optional<OdontogramTooth> findByPatientIdAndToothNumber(UUID patientId, Short toothNumber);
}
