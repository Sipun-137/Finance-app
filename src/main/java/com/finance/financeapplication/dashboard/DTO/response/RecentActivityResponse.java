package com.finance.financeapplication.dashboard.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecentActivityResponse {
    private List<RecentActivityItem> activities;
    private int count;
}
