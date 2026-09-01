package com.odontosystem.api.repository;

import com.odontosystem.api.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DentistRepository extends JpaRepository<Dentist, UUID> {
    List<Dentist> findByDistrictIgnoreCase(String district);
}
