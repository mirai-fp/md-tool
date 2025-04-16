package com.example.mdtool.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalysisPBResult {
    private double totalSalesAmount; // 売上合計
    private double totalWholeSales; // 原価

    private double profitRate; // 利益率

    private double totalQuantityCurrentWeek;  // 売上数合計（当週）
    private double totalQuantityPreviousWeek;  // 売上数合計（先週）
    private double totalQuantityChangeRate; // 売上数 前週比

    private double salesAmountChangeRate; // 売上構成比
}
