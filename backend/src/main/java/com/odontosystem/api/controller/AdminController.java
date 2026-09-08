package com.odontosystem.api.controller;

import com.odontosystem.api.dto.AdminDtos.*;
import com.odontosystem.api.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserAdminDto> users() { return adminService.listUsers(); }

    @PostMapping("/users")
    public ResponseEntity<UserAdminDto> create(Authentication auth, @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(auth.getName(), request));
    }

    @PutMapping("/users/{id}")
    public UserAdminDto update(Authentication auth, @PathVariable UUID id,
                               @Valid @RequestBody UpdateUserRequest request) {
        return adminService.updateUser(auth.getName(), id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable UUID id) {
        adminService.deleteUser(auth.getName(), id);
    }

    @GetMapping("/audit-logs")
    public List<AuditDto> auditLogs() { return adminService.auditLogs(); }
}