package com.odontosystem.api.repository;

import com.odontosystem.api.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByPatientIdOrderByDateDescTimeDesc(UUID patientId);

    Optional<Appointment> findByIdAndPatientId(UUID id, UUID patientId);

    List<Appointment> findByDentistIdOrderByDateDescTimeDesc(UUID dentistId);

    Optional<Appointment> findByIdAndDentistId(UUID id, UUID dentistId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM dentist_schedules s "
            + "WHERE s.dentist_id = :dentistId "
            + "AND s.day_of_week = EXTRACT(ISODOW FROM CAST(:date AS date)) "
            + "AND CAST(:time AS time) >= s.start_time "
            + "AND CAST(:time AS time) < s.end_time "
            + "AND MOD(EXTRACT(EPOCH FROM (CAST(:time AS time) - s.start_time)) / 60, s.slot_minutes) = 0)",
            nativeQuery = true)
    boolean existsAvailableSchedule(@Param("dentistId") UUID dentistId,
                                    @Param("date") LocalDate date,
                                    @Param("time") LocalTime time);
}
