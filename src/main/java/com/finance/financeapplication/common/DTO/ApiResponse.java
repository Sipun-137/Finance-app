package com.finance.financeapplication.common.DTO;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    // Static helper methods for convenience
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }

}
