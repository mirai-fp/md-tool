package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class StockTableRow {
    private String color;
    private String size;
    private Map<String, Integer> weeklyStock; // key: "2024-W39"
}
