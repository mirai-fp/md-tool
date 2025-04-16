package com.example.mdtool.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalysisOverallResult {
    String parentProductType;
    String childProductType;
    double monthlyAmount;
    double previousMonthlyAmount;
    double previousMonthlyAmountDiff;
    double previousMonthlyAmountRate;
}
