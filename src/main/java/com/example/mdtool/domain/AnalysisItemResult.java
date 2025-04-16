package com.example.mdtool.domain;

import lombok.Data;

@Data
public class AnalysisItemResult {
    private String brandCode;
    private String productName;

    private double totalSales;      // 売上金額合計

    private double totalSalesAmount;      // 売上数合計

    private double stock;

    private double profit; // 利益

    private double totalWholeSales; // 原価合計
    private double orderAmount; // 発注数(10wk)
    private double expectedOrderAmount; // 発注予定(10週先まで)
    private double totalSalesAmount3Week;      // 売上数合計(3wk)
    private double totalSalesAmount10Week;      // 売上数合計(10wk)
}
