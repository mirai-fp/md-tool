package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class OrderTableRow {
    private String color;
    private String size;
    private Map<String, Integer> weeklyOrder; // key: "2024-W39"
}
