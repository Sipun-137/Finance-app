package com.finance.financeapplication.user.controller;


import com.finance.financeapplication.audit.annotation.Auditable;
import com.finance.financeapplication.common.DTO.ApiResponse;
import com.finance.financeapplication.common.enums.Role;
import com.finance.financeapplication.common.enums.UserStatus;
import com.finance.financeapplication.user.DTO.request.AssignRoleRequest;
import com.finance.financeapplication.user.DTO.request.UserRequestDTO;
import com.finance.financeapplication.user.DTO.request.UserUpdateDTO;
import com.finance.financeapplication.user.DTO.response.UserResponse;
import com.finance.financeapplication.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "CREATE_USER", resource = "users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequestDTO request) {

        log.info("Admin creating new user with email: {}", request.getEmail());
        UserResponse created = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "VIEW_USERS", resource = "users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) UserStatus status) {

        List<UserResponse> users = (status != null)
                ? userService.findByStatus(status)
                : userService.findAllUsers();

        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Auditable(action = "VIEW_USER_BY_ID", resource = "users")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable String id) {

        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", user));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "UPDATE_USER", resource = "users")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateDTO request) {

        log.info("Updating user with id: {}", id);
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "DEACTIVATE_USER", resource = "users")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable String id) {
        log.info("Deactivating user with id: {}", id);
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "ACTIVATE_USER", resource = "users")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable String id) {
        log.info("Activating user with id: {}", id);
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", null));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "ASSIGN_ROLE", resource = "users")
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(
            @PathVariable String id,
            @Valid @RequestBody AssignRoleRequest request) {

        log.info("Assigning role {} to user {}", request.getRoleName(), id);
        UserResponse updated = userService.assignRole(id, request.getRoleName());
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", updated));
    }

    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "REMOVE_ROLE", resource = "users")
    public ResponseEntity<ApiResponse<UserResponse>> removeRole(
            @PathVariable String id,
            @RequestParam Role role) {

        log.info("Removing role {} from user {}", role, id);
        UserResponse updated = userService.removeRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", updated));
    }
}
