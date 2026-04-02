package com.finance.financeapplication.record.service;

import com.finance.financeapplication.common.DTO.PagedResponse;
import com.finance.financeapplication.record.DTO.request.CreateRecordRequest;
import com.finance.financeapplication.record.DTO.request.RecordFilterRequest;
import com.finance.financeapplication.record.DTO.request.UpdateRecordRequest;
import com.finance.financeapplication.record.DTO.response.RecordResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FinancialRecordService {

    RecordResponse createRecord(String userId, CreateRecordRequest request);

    RecordResponse findById(String recordId, String userId);

    PagedResponse<RecordResponse> findAllRecords(String userId, RecordFilterRequest filter, Pageable pageable);

    RecordResponse updateRecord(String recordId, String userId, UpdateRecordRequest request);

    void deleteRecord(String recordId, String userId);

    List<RecordResponse> findRecentRecords(String userId, int limit);


}
