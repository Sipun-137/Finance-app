package com.finance.financeapplication.user.DTO.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;   // null means "don't change"

    @Email(message = "Invalid email format")
    private String email;
}
