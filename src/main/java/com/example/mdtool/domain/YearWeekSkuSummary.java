package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class YearWeekSkuSummary {
    private int year;
    private int week;
    private String color;
    private String size;
    private int totalQuantity;

    public String getYearWeek() {
        return String.format("%04d-W%02d", year, week);
    }
}
