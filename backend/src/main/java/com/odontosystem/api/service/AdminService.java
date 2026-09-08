package com.odontosystem.api.service;

import com.odontosystem.api.dto.AdminDtos.*;
import com.odontosystem.api.entity.AuditLog;
import com.odontosystem.api.entity.User;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.AuditLogRepository;
import com.odontosystem.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserAdminDto> listUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public UserAdminDto createUser(String actorEmail, CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Ya existe una cuenta registrada con ese correo");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .build();
        userRepository.save(user);
        audit(actorEmail, "CREATE", "USER", user.getId());
        return toDto(user);
    }

    @Transactional
    public UserAdminDto updateUser(String actorEmail, UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        audit(actorEmail, "UPDATE", "USER", id);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(String actorEmail, UUID id) {
        if (!userRepository.existsById(id)) {
            throw ApiException.notFound("Usuario no encontrado");
        }
        userRepository.deleteById(id);
        audit(actorEmail, "DELETE", "USER", id);
    }

    @Transactional(readOnly = true)
    public List<AuditDto> auditLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(log -> AuditDto.builder().id(log.getId()).actorEmail(log.getActorEmail())
                        .action(log.getAction()).resource(log.getResource()).resourceId(log.getResourceId())
                        .createdAt(log.getCreatedAt()).build()).toList();
    }

    private void audit(String actorEmail, String action, String resource, UUID resourceId) {
        auditLogRepository.save(AuditLog.builder().actorEmail(actorEmail).action(action)
                .resource(resource).resourceId(resourceId).build());
    }

    private UserAdminDto toDto(User user) {
        return UserAdminDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail())
                .role(user.getRole()).phone(user.getPhone()).build();
    }
}