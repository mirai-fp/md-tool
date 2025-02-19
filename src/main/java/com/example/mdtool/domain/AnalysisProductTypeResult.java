package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AnalysisProductTypeResult {
    private String childProductType;       // 子商品タイプ
    private double salesAmount;      // 売上金額合計
    private double salesAmountChangeRate; // 売上金額の前週比率
    private double quantity;            // 売上数量合計
    private double quantityChangeRate;    // 売上数量の前週比率
    private double salesCompositionRate;  // 売上構成比
    private double stock;               // 在庫
    private double stockWholesalePrice; // 在庫原価
    private double profit;      // 粗利
    private double previousYearProfit;  // 昨年粗利率
    private double profitRate;  // 粗利率
    private double previousYearProfitRate;  // 昨年粗利率
}
