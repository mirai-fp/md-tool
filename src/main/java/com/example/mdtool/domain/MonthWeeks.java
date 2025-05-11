package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class MonthWeeks {
    private String label; // 例: "2024年10月"
    private List<String> weekNumbers;
}
