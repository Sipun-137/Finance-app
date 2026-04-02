package com.finance.financeapplication.auth.controller;


import com.finance.financeapplication.audit.annotation.Auditable;
import com.finance.financeapplication.auth.DTO.LoginRequestDTO;
import com.finance.financeapplication.auth.model.UserPrincipal;
import com.finance.financeapplication.auth.service.JwtService;
import com.finance.financeapplication.common.DTO.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;


    @PostMapping("/login")
    @Auditable(action = "USER_LOGIN", resource = "users", preAuth = true)
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequestDTO dto) {

        try {
            log.info("Login attempt for user: {}", dto.getUserName());
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(dto.getUserName(), dto.getPassword()));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("id", principal.getId());
            data.put("name", principal.getName());
            data.put("email", principal.getUsername());
            data.put("roles", principal.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());

            log.info("Login successful for user: {}", dto.getUserName());
            return ResponseEntity.ok(ApiResponse.success("Login Successful", data));

        }catch(DisabledException e){
            log.warn("Login attempt for disabled account: {}", dto.getUserName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.failure("Account is inactive. Please contact admin."));
        }
         catch (BadCredentialsException e) {
             log.warn("Bad credentials for user: {}", dto.getUserName());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                     .body(ApiResponse.failure("Invalid email or password."));
        }
    }
}
