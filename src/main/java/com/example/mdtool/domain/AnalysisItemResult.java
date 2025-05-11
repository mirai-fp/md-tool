package com.example.mdtool.domain;

import lombok.Data;

@Data
public class AnalysisItemResult {
    private String brandCode;
    private String productName;
    private double wholesalePrice;
    private double retailPrice;

    private double totalSales;      // 売上金額合計

    private double totalSalesAmount;      // 売上数合計

    private double stock;

    private double profit; // 利益

    private double totalWholeSales; // 原価合計
    private double orderAmount; // 発注数(10wk)
    private double expectedOrderAmount; // 発注予定(10週先まで)
    private double totalSalesAmount1WeekAgo;      // 売上数合計(1wk前)
    private double totalSalesAmount2WeekAgo;      // 売上数合計(2wk前)

    private double favoriteCount; // お気に入り数（当週）
    private double favoriteCount1WeekAgo; // お気に入り数（先週）
}
