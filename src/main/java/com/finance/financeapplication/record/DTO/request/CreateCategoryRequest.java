package com.finance.financeapplication.record.DTO.request;

import com.finance.financeapplication.common.enums.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Type is required — must be INCOME or EXPENSE")
    private RecordType type;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$",
            message = "Color must be a valid hex code e.g. #FF5733")
    private String color;
}