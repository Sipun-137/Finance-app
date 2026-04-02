package com.finance.financeapplication.user.service;


import com.finance.financeapplication.common.enums.Role;
import com.finance.financeapplication.common.enums.UserStatus;
import com.finance.financeapplication.user.DTO.request.UserRequestDTO;
import com.finance.financeapplication.user.DTO.request.UserUpdateDTO;
import com.finance.financeapplication.user.DTO.response.UserResponse;
import com.finance.financeapplication.user.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // Create a new user — used by admin or during registration
    UserResponse createUser(UserRequestDTO request);

    // Fetch single user by ID — throws ResourceNotFoundException if absent
    UserResponse findById(String id);

    // Fetch single user by email — used internally by auth flow
    User findByEmail(String email);

    // Fetch all users — admin only
    List<UserResponse> findAllUsers();

    // Fetch users filtered by their status
    List<UserResponse> findByStatus(UserStatus status);

    // Update name or email — password update is a separate flow
    UserResponse updateUser(String id, UserUpdateDTO request);

    // Soft deactivation — sets status to INACTIVE, does not delete
    void deactivateUser(String id);

    // Re-activate a previously deactivated user
    void activateUser(String id);

    // Assign a role to a user — admin only
    UserResponse assignRole(String userId, Role roleName);

    // Remove a role from a user — admin only
    UserResponse removeRole(String userId, Role roleName);

    User findEntityById(String id);
}