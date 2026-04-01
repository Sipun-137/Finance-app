package com.finance.financeapplication.config.security;


import com.finance.financeapplication.common.enums.Role;
import com.finance.financeapplication.user.DTO.request.UserRequestDTO;
import com.finance.financeapplication.user.repo.UserRepository;
import com.finance.financeapplication.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    @Value("${app.security.admin.email}")
    private String adminEmail;

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.findByEmail(adminEmail).isEmpty())
        {
            log.info("No users found in database. Creating default admin user.");

            UserRequestDTO user = UserRequestDTO.builder()
                    .name("Admin")
                    .email(adminEmail)
                    .password("Admin#1234")
                    .role(Role.ROLE_ADMIN)
                    .build();
            try {
                userService.createUser(
                        user);
                log.info("Default admin user created successfully.");
            } catch (Exception e) {
                log.error("Failed to create default admin user: {}", e.getMessage());
            }
        }else{
            log.info("Admin user already exists.");
        }
    }
}
