package com.finance.financeapplication.record.DTO.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$",
            message = "Color must be a valid hex code e.g. #FF5733")
    private String color;
}
