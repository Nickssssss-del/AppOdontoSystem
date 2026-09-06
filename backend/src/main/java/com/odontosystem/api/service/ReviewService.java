package com.odontosystem.api.service;

import com.odontosystem.api.dto.ReviewDtos.CreateReviewRequest;
import com.odontosystem.api.dto.ReviewDtos.ReviewDto;
import com.odontosystem.api.entity.Appointment;
import com.odontosystem.api.entity.AppointmentStatus;
import com.odontosystem.api.entity.Dentist;
import com.odontosystem.api.entity.Review;
import com.odontosystem.api.entity.User;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.AppointmentRepository;
import com.odontosystem.api.repository.DentistRepository;
import com.odontosystem.api.repository.ReviewRepository;
import com.odontosystem.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewDto create(String patientEmail, CreateReviewRequest request) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        Appointment appointment = appointmentRepository.findByIdAndPatientId(request.getAppointmentId(), patient.getId())
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.Completada) {
            throw ApiException.badRequest("Solo puedes calificar citas que ya fueron completadas");
        }

        if (reviewRepository.existsByAppointmentId(appointment.getId())) {
            throw ApiException.conflict("Esta cita ya tiene una reseña registrada");
        }

        Review review = Review.builder()
                .dentist(appointment.getDentist())
                .patient(patient)
                .appointment(appointment)
                .rating(request.getRating().shortValue())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
        recalculateDentistRating(appointment.getDentist().getId());

        return toDto(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> findByDentist(UUID dentistId) {
        if (!dentistRepository.existsById(dentistId)) {
            throw ApiException.notFound("Odontólogo no encontrado");
        }

        return reviewRepository.findByDentistIdOrderByCreatedAtDesc(dentistId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private void recalculateDentistRating(UUID dentistId) {
        List<Review> reviews = reviewRepository.findByDentistIdOrderByCreatedAtDesc(dentistId);

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> ApiException.notFound("Odontólogo no encontrado"));

        dentist.setRating((float) Math.round(average * 10) / 10f);
        dentist.setReviewsCount(reviews.size());
        dentistRepository.save(dentist);
    }

    private ReviewDto toDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .dentistId(review.getDentist().getId())
                .appointmentId(review.getAppointment().getId())
                .patientName(review.getPatient().getName())
                .rating(review.getRating().intValue())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}