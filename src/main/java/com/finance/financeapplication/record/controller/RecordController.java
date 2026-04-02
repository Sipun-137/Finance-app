package com.finance.financeapplication.record.controller;


import com.finance.financeapplication.audit.annotation.Auditable;
import com.finance.financeapplication.auth.model.UserPrincipal;
import com.finance.financeapplication.common.DTO.ApiResponse;
import com.finance.financeapplication.common.DTO.PagedResponse;
import com.finance.financeapplication.record.DTO.request.CreateRecordRequest;
import com.finance.financeapplication.record.DTO.request.RecordFilterRequest;
import com.finance.financeapplication.record.DTO.request.UpdateRecordRequest;
import com.finance.financeapplication.record.DTO.response.RecordResponse;
import com.finance.financeapplication.record.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/record")
@RequiredArgsConstructor
@Tag(name = "Record APIs", description = "Record related APIs")
public class RecordController {
    private final FinancialRecordService service;

    private String getUserId(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Auditable(action = "CREATE_RECORD", resource = "record")
    @Operation(summary = "Create a new record")
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            Authentication authentication,
            @RequestBody @Valid CreateRecordRequest request
    ) {

        RecordResponse response = service.createRecord(getUserId(authentication), request);

        return ResponseEntity.ok(
                ApiResponse.<RecordResponse>builder()
                        .success(true)
                        .message("Record created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Auditable(action = "VIEW_RECORD_BY_ID", resource = "record")
    @Operation(summary = "Get a record by ID")
    public ResponseEntity<ApiResponse<RecordResponse>> getById(
            @PathVariable String id,
            Authentication authentication
    ) {
        RecordResponse response = service.findById(id, getUserId(authentication));

        return ResponseEntity.ok(
                ApiResponse.<RecordResponse>builder()
                        .success(true)
                        .message("Record retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Auditable(action = "VIEW_RECORDS", resource = "record")
    @Operation(summary = "Get all records")
    public ResponseEntity<ApiResponse<PagedResponse<RecordResponse>>> getAllRecords(
            Authentication authentication,
            RecordFilterRequest filter,
            @PageableDefault Pageable pageable
            ) {

        if (filter == null) {
            filter = new RecordFilterRequest();
        }

        PagedResponse<RecordResponse> response =
                service.findAllRecords(getUserId(authentication), filter, pageable);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<RecordResponse>>builder()
                        .success(true)
                        .message("Records fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Auditable(action = "UPDATE_RECORD", resource = "record")
    @Operation(summary = "Update a record")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @PathVariable String id,
            Authentication authentication,
            @RequestBody @Valid UpdateRecordRequest request
    ) {
        RecordResponse response = service.updateRecord(id, getUserId(authentication), request);

        return ResponseEntity.ok(
                ApiResponse.<RecordResponse>builder()
                        .success(true)
                        .message("Record updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Auditable(action = "DELETE_RECORD", resource = "record")
    @Operation(summary = "Delete a record")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable String id,
            Authentication authentication
    ) {
        service.deleteRecord(id, getUserId(authentication));

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Record deleted successfully")
                        .build()
        );
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Auditable(action = "VIEW_RECENT_RECORDS", resource = "record")
    @Operation(summary = "Get recent records")
    public ResponseEntity<ApiResponse<List<RecordResponse>>> getRecentRecords(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<RecordResponse> response = service.findRecentRecords(getUserId(authentication), limit);

        return ResponseEntity.ok(
                ApiResponse.<List<RecordResponse>>builder()
                        .success(true)
                        .message("Recent records fetched successfully")
                        .data(response)
                        .build()
        );
    }



}
