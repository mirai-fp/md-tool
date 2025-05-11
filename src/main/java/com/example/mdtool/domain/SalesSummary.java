package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SalesSummary {
    private String productName;
    private double totalSales;
    private double totalQuantity;
    private double retailPrice;
}
