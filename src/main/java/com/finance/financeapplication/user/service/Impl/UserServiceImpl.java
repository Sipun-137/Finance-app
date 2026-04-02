package com.finance.financeapplication.user.service.Impl;

import com.finance.financeapplication.common.enums.Role;
import com.finance.financeapplication.common.enums.UserStatus;
import com.finance.financeapplication.exception.common.BadRequestException;
import com.finance.financeapplication.exception.common.ResourceNotFoundException;
import com.finance.financeapplication.user.DTO.request.UserRequestDTO;
import com.finance.financeapplication.user.DTO.request.UserUpdateDTO;
import com.finance.financeapplication.user.DTO.response.UserResponse;
import com.finance.financeapplication.user.model.User;
import com.finance.financeapplication.user.repo.UserRepository;
import com.finance.financeapplication.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // never store plain text
                .status(UserStatus.ACTIVE)
                .build();

        user.getRoles().add(request.getRole()!=null?request.getRole(): Role.ROLE_VIEWER);  // add to the Set<Role>

        User saved = userRepository.save(user);
        log.info("Created new user: {} with role: {}", saved.getEmail(), user.getRoles());

        return toResponse(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(String id) {
        User user = userRepository.findByIdWithRoles(id)   // single query with JOIN FETCH
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        // Returns raw entity — used by auth flow internally, not the controller
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        return userRepository.findAllWithRoles()  // avoids N+1 — one query loads all roles
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(String id, UserUpdateDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!user.getEmail().equals(request.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        log.info("Updated user: {}", user.getEmail());
        return toResponse(user);
    }

    @Override
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("User is already inactive");
        }

        user.setStatus(UserStatus.INACTIVE);
        log.info("Deactivated user: {}", user.getEmail());
    }

    @Override
    public void activateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("User is already active");
        }

        user.setStatus(UserStatus.ACTIVE);
        log.info("Activated user: {}", user.getEmail());
    }

    @Override
    public UserResponse assignRole(String userId, Role roleName) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Guard: don't add the same role twice
        boolean alreadyHasRole = user.getRoles().stream()
                .anyMatch(r -> r == roleName);

        if (alreadyHasRole) {
            throw new BadRequestException("User already has role: " + roleName);
        }

        user.getRoles().add(roleName);
        log.info("Assigned role {} to user {}", roleName, user.getEmail());

        return toResponse(user);
    }

    @Override
    public UserResponse removeRole(String userId, Role roleName) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Guard: must have at least one role at all times
        if (user.getRoles().size() <= 1) {
            throw new BadRequestException("Cannot remove the only role. Assign another role first.");
        }

        boolean removed = user.getRoles().removeIf(r -> r== roleName);

        if (!removed) {
            throw new BadRequestException("User does not have role: " + roleName);
        }

        log.info("Removed role {} from user {}", roleName, user.getEmail());
        return toResponse(user);
    }

    @Override
    public User findEntityById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(Enum::name)   // e.g. "ROLE_ADMIN"
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
