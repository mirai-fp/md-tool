package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AnalysisResult {
    private List<AnalysisProductTypeResult> analysisProductTypeResult;

    private List<AnalysisItemResult> analysisItemResult;
}