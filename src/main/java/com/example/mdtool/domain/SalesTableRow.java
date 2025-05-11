package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class SalesTableRow {
    private String color;
    private String size;
    private Map<String, Integer> weeklySales; // key: "2024-W39"
}
