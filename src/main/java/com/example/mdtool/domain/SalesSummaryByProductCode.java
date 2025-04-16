package com.example.mdtool.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesSummaryByProductCode {
    private String parentProductType;
    private double amount;
    private double quantity;
    private double wholesalePrice;
    private double stock;
    private double stockWholesalePrice;
}
