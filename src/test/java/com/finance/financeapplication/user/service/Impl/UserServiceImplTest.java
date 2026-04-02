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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_whenRoleProvided_shouldSaveEncodedPasswordAndRole() {
        UserRequestDTO request = UserRequestDTO.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password("Secret#123")
                .role(Role.ROLE_ADMIN)
                .build();

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret#123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-1");
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertEquals("user-1", response.getId());
        assertEquals("admin@example.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed-password", captor.getValue().getPasswordHash());
        assertEquals(UserStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void createUser_whenRoleMissing_shouldDefaultToViewerRole() {
        UserRequestDTO request = UserRequestDTO.builder()
                .name("Viewer User")
                .email("viewer@example.com")
                .password("Secret#123")
                .build();

        when(userRepository.existsByEmail("viewer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret#123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-2");
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertEquals("user-2", response.getId());
        assertTrue(response.getRoles().contains("ROLE_VIEWER"));
    }

    @Test
    void createUser_whenEmailAlreadyExists_shouldThrowBadRequest() {
        UserRequestDTO request = UserRequestDTO.builder()
                .name("Duplicate")
                .email("duplicate@example.com")
                .password("Secret#123")
                .role(Role.ROLE_ANALYST)
                .build();

        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.createUser(request));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_whenEmailAlreadyInUse_shouldThrowBadRequest() {
        User existing = User.builder()
                .id("user-1")
                .name("Existing")
                .email("existing@example.com")
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(Role.ROLE_VIEWER)))
                .build();

        UserUpdateDTO request = new UserUpdateDTO("Updated Name", "taken@example.com");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateUser("user-1", request));
    }

    @Test
    void deactivateUser_whenAlreadyInactive_shouldThrowBadRequest() {
        User inactive = User.builder()
                .id("user-1")
                .email("inactive@example.com")
                .status(UserStatus.INACTIVE)
                .roles(new HashSet<>(Set.of(Role.ROLE_VIEWER)))
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(inactive));

        assertThrows(BadRequestException.class, () -> userService.deactivateUser("user-1"));
    }

    @Test
    void assignRole_whenRoleAlreadyAssigned_shouldThrowBadRequest() {
        User user = User.builder()
                .id("user-1")
                .email("user@example.com")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(Role.ROLE_ANALYST)))
                .build();

        when(userRepository.findByIdWithRoles("user-1")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.assignRole("user-1", Role.ROLE_ANALYST));
    }

    @Test
    void removeRole_whenOnlyOneRole_shouldThrowBadRequest() {
        User user = User.builder()
                .id("user-1")
                .email("user@example.com")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(Role.ROLE_VIEWER)))
                .build();

        when(userRepository.findByIdWithRoles("user-1")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.removeRole("user-1", Role.ROLE_VIEWER));
    }

    @Test
    void removeRole_whenMultipleRoles_shouldRemoveRoleAndReturnUpdatedResponse() {
        User user = User.builder()
                .id("user-1")
                .name("Test User")
                .email("user@example.com")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(Role.ROLE_ADMIN, Role.ROLE_ANALYST)))
                .build();

        when(userRepository.findByIdWithRoles("user-1")).thenReturn(Optional.of(user));

        UserResponse response = userService.removeRole("user-1", Role.ROLE_ANALYST);

        assertEquals("user-1", response.getId());
        assertEquals(Set.of("ROLE_ADMIN"), response.getRoles());
    }

    @Test
    void findById_whenUserMissing_shouldThrowResourceNotFound() {
        when(userRepository.findByIdWithRoles("missing-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById("missing-id"));
    }
}
