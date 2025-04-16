package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AnalysisProductTypeResult {
    private String parentProductType;
    private String childProductType;    // 子商品タイプ

    private double salesAmount;      // 売上実績
    private double previousSalesAmount;      // 前週対比金額
    private double salesAmountChangeRate; // 売上金額前週比
    private double previousQuantity;      // 前週対比数量

    private double profit;      // 粗利
    private double previousYearProfit;  // 昨年粗利
    private double profitRate;  // 今期粗利率
    private double previousYearProfitRate;  // 昨年粗利率

    private double quantity;            // 売上数量合計
    private double quantityChangeRate;    // 売上数量の前週比率

    private double stockWholesalePrice; // 在庫原価

    private double stock;               // 在庫数量

    private double salesCompositionRate; // 売上構成比

    //在庫週数
    public double getStockWeek() {
        return quantity != 0 ? stock / quantity : 0;
    }

    //今期平均単価
    public double getAverageAmount() {
        return salesAmount != 0 ? salesAmount / quantity : 0;
    }

}
