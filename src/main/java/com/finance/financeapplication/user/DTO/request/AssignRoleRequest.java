package com.finance.financeapplication.user.DTO.request;

import com.finance.financeapplication.common.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotNull(message = "Role name is required")
    private Role roleName;
}
