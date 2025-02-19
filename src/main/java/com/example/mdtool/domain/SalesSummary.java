package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalesSummary {
    private String productName;
    private double totalSales;
    private double totalQuantity;
}
