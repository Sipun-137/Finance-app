package com.finance.financeapplication.user.repo;

import com.finance.financeapplication.common.enums.UserStatus;
import com.finance.financeapplication.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    // Used during registration to prevent duplicate accounts
    boolean existsByEmail(String email);

    // Used by admin to filter users by status (ACTIVE / INACTIVE)
    List<User> findByStatus(UserStatus status);

    // Fetch user with roles in a single query — avoids N+1 problem
    // Without this, accessing user.getRoles() triggers a separate DB call per user
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(String id);

    // Used for admin dashboard — all users with their roles loaded
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRoles();
}
