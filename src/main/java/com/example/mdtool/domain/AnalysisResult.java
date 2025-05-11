package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {
    // 月次集計
    private Map<String, List<AnalysisOverallResult>> analysisOverallResult;

    // PB比率
    private Map<String, AnalysisPBResult> analysisPBResult;

    // 子タイプ別の週次集計
    private Map<String, List<AnalysisProductTypeResult>> analysisProductTypeResult;

    //　商品別集計
    private List<AnalysisItemResult> analysisItemResult;

    // 表示対象の週
    private int week;

    // 売上合計を取得
    public double getTotalMonthlyAmount() {
        return analysisOverallResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisOverallResult::getMonthlyAmount)
                .sum();
    }

    // 昨年売上合計を取得
    public double getTotalPreviousMonthlyAmount() {
        return analysisOverallResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisOverallResult::getPreviousMonthlyAmount)
                .sum();
    }

    // 昨年対比差額合計を取得
    public double getTotalPreviousMonthlyAmountDiff() {
        return getTotalMonthlyAmount() - getTotalPreviousMonthlyAmount();
    }

    // 昨年対比売上 (%) を取得
    public double getTotalPreviousMonthlyAmountRate() {
        double totalPrevious = getTotalPreviousMonthlyAmount();
        return totalPrevious != 0 ? (getTotalMonthlyAmount() / totalPrevious) * 100 : 0;
    }

    // 週次の合計値を計算するメソッド
    public Map<String, Double> getProductTypeTotals() {
        Map<String, Double> totals = new HashMap<>();

        // 売上金額合計
        double totalSalesAmount = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getSalesAmount)
                .sum();
        totals.put("totalSalesAmount", totalSalesAmount);

        // 売上金額比
        double totalPreviousSalesAmount = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getPreviousSalesAmount)
                .sum();
        totals.put("totalSalesAmountChangeRate", totalSalesAmount / totalPreviousSalesAmount * 100);

        // 売上数量
        double totalQuantity = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getQuantity)
                .sum();
        totals.put("totalQuantity", totalQuantity);

        // 売上数量比率
        double totalPreviousQuantity = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getPreviousQuantity)
                .sum();
        totals.put("totalQuantityChangeRate", totalQuantity / totalPreviousQuantity * 100);

        // 粗利合計
        double totalProfit = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getProfit)
                .sum();
        totals.put("totalProfit", totalProfit);

        // 粗利率合計 (平均の粗利率を求める場合)
        double totalProfitRate = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getProfitRate)
                .average()
                .orElse(0.0);
        totals.put("totalProfitRate", totalProfit / totalSalesAmount);

        // 昨年粗利合計
        double totalPreviousYearProfit = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getPreviousSalesAmount)
                .sum();
        totals.put("totalPreviousYearProfit", totalPreviousYearProfit);

        // 昨年粗利率合計 (平均)
        double totalPreviousYearAmount = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getPreviousSalesAmount)
                .average()
                .orElse(0.0);
        totals.put("totalPreviousYearProfitRate", totalSalesAmount / totalPreviousYearAmount);

        // 在庫原価合計
        double totalStockWholesalePrice = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getStockWholesalePrice)
                .sum();
        totals.put("totalStockWholesalePrice", totalStockWholesalePrice);

        // 在庫合計
        double totalStock = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getStock)
                .sum();
        totals.put("totalStock", totalStock);

        // 在庫週数合計
        totals.put("totalStockWeek", totalStock / totalQuantity);

        // 平均単価合計
        totals.put("totalAverageAmount", totalSalesAmount / totalQuantity);

        // 売上構成比合計
        double totalSalesCompositionRate = analysisProductTypeResult.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AnalysisProductTypeResult::getSalesCompositionRate)
                .sum();
        totals.put("totalSalesCompositionRate", totalSalesCompositionRate);

        return totals;
    }

    public int getTotalAmountByOverallResult(String productType) {
        return (int) analysisOverallResult.get(productType)
                .stream()
                .mapToDouble(AnalysisOverallResult::getMonthlyAmount)
                .sum();
    }

    public double getTotalAmountRateByOverallResult(String productType) {
        return (int) analysisOverallResult.get(productType)
                .stream()
                .mapToDouble(AnalysisOverallResult::getMonthlyAmount)
                .sum()
                /
                getTotalMonthlyAmount() * 100;
    }

    public int getTotalAmountByProductTypeResult(String productType) {
        return (int) analysisProductTypeResult.get(productType)
                .stream()
                .mapToDouble(AnalysisProductTypeResult::getSalesAmount)
                .sum();
    }

    public double getTotalAmountRateByProductTypeResult(String productType) {
        return analysisProductTypeResult.get(productType)
                .stream()
                .mapToDouble(AnalysisProductTypeResult::getSalesAmount)
                .sum()
                /
                getProductTypeTotals().get("totalSalesAmount") * 100;
    }
}