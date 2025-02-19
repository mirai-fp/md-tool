package com.example.mdtool.service;

import com.example.mdtool.domain.*;
import com.example.mdtool.repository.OrderDataRepository;
import com.example.mdtool.repository.SaleDataRepository;
import com.example.mdtool.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    @Autowired
    private final SaleDataRepository saleDataRepository;

    @Autowired
    private final StockDataRepository stockDataRepository;

    @Autowired
    private final OrderDataRepository orderDataRepository;

    public AnalysisResult analyze(LocalDate startDate, LocalDate endDate) {
        List<SalesData> currentWeekData = saleDataRepository.findByDateRange(startDate, endDate);
        List<SalesData> previousWeekData = saleDataRepository.findByDateRange(startDate.minusWeeks(1), endDate.minusWeeks(1));
        List<SalesData> previousYearData = saleDataRepository.findByDateRange(startDate.minusYears(1), endDate.minusYears(1));

        // 現在週と前週のデータを子商品タイプごとに集計
        Map<String, SalesSummaryByProductCode> currentWeekSalesSummaryMap = getSalesSummaryByProductCode(currentWeekData);
        Map<String, SalesSummaryByProductCode> previousWeekSalesSummaryMap = getSalesSummaryByProductCode(previousWeekData);
        Map<String, SalesSummaryByProductCode> previousYearSalesSummaryMap = getSalesSummaryByProductCode(previousYearData);

        // 分析結果をリストにまとめる
        List<AnalysisProductTypeResult> analysisResults = new ArrayList<>();
        for (String childProductType : currentWeekSalesSummaryMap.keySet()) {
            SalesSummaryByProductCode currentWeekSalesSummary = currentWeekSalesSummaryMap.get(childProductType);
            SalesSummaryByProductCode previousWeekSalesSummary = nonNull(previousWeekSalesSummaryMap.get(childProductType)) ? previousWeekSalesSummaryMap.get(childProductType) : SalesSummaryByProductCode.builder().build();
            SalesSummaryByProductCode previousYearSalesSummary = nonNull(previousYearSalesSummaryMap.get(childProductType)) ? previousYearSalesSummaryMap.get(childProductType) : SalesSummaryByProductCode.builder().build();

            double currentAmount = currentWeekSalesSummary.getAmount();
            double currentQuantity = currentWeekSalesSummary.getQuantity();
            double previousAmount = previousWeekSalesSummary.getAmount();
            double previousQuantity = previousWeekSalesSummary.getQuantity();
            double previousYearAmount = previousYearSalesSummary.getAmount();

            // 前週比率計算
            double salesAmountChangeRate = previousAmount != 0 ? (currentAmount - previousAmount) / previousAmount * 100 : 0;
            double quantityChangeRate = previousQuantity != 0 ? (currentQuantity - previousQuantity) / previousQuantity * 100 : 0;

            // 売上構成比
            double totalCurrentAmount = currentWeekSalesSummaryMap.values().stream()
                    .mapToDouble(SalesSummaryByProductCode::getAmount)
                    .sum();
            double salesCompositionRate = totalCurrentAmount != 0 ? (currentAmount / totalCurrentAmount) * 100 : 0;

            // 分析結果の追加
            analysisResults.add(AnalysisProductTypeResult.builder()
                    .childProductType(childProductType)
                    .salesAmount(currentAmount)
                    .salesAmountChangeRate(salesAmountChangeRate)
                    .salesCompositionRate(salesCompositionRate)
                    .quantityChangeRate(quantityChangeRate)
                    .quantity(currentWeekSalesSummary.getQuantity())
                    .stock(currentWeekSalesSummary.getStock())
                    .stockWholesalePrice(currentWeekSalesSummary.getStockWholesalePrice())
                    .profit(currentWeekSalesSummary.getAmount() - currentWeekSalesSummary.getWholesalePrice())
                    .profitRate((currentWeekSalesSummary.getAmount() - currentWeekSalesSummary.getWholesalePrice()) / currentAmount * 100)
                            .previousYearProfit(previousYearSalesSummary.getAmount() - previousYearSalesSummary.getWholesalePrice())
                            .previousYearProfitRate((previousYearSalesSummary.getAmount() - previousYearSalesSummary.getWholesalePrice()) / previousYearAmount * 100)
                    .build());
        }

        // ブランド品番ごとに集計
        List<AnalysisItemResult> analysisItemResults = currentWeekData.stream()
                .collect(Collectors.groupingBy(
                        SalesData::getBrandCode,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    String productName = list.stream().map(SalesData::getProductName).findFirst().orElse("Unknown");
                                    double totalSales = list.stream().mapToDouble(SalesData::getTotalAmount).sum();
                                    double totalQuantity = list.stream().mapToInt(SalesData::getOrderQuantity).sum();
                                    return new SalesSummary(productName, totalSales, totalQuantity);
                                }
                        )
                ))
                .entrySet().stream()
                .map(entry -> {
                    AnalysisItemResult result = new AnalysisItemResult();
                    result.setBrandCode(entry.getKey());       // BrandCode
                    result.setProductName(entry.getValue().getProductName());  // ProductName
                    result.setTotalSales(entry.getValue().getTotalSales());
                    result.setTotalSalesAmount(entry.getValue().getTotalQuantity());
                    result.setStock(stockDataRepository.findByBrandCode(entry.getKey())
                            .map(stockDataList -> stockDataList.stream()
                                    .mapToDouble(StockData::getStock)
                                    .sum()
                            )
                            .orElse(0.0)
                    );
                    Optional<List<OrderData>> orderDataList = orderDataRepository.findByBrandCode(result.getBrandCode().split("/")[0]);
                    result.setOrderAmount(orderDataList
                            .map(list -> list.stream().mapToInt(OrderData::getOrderQuantity).sum())
                            .orElse(0)
                    );
                    result.setTotalWholeSales(orderDataList
                            .map(odl -> odl.stream()
                                    .findFirst()
                                    .map(o -> o.getWholesalePrice())
                                    .orElse(0)
                            )
                            .orElse(0) * result.getTotalSalesAmount()
                    );
                    result.setProfit(result.getTotalSales() - result.getTotalWholeSales());
                    return result;
                })
                .collect(Collectors.toList());

        analysisResults.sort((o1, o2) -> Double.compare(o2.getSalesAmount(), o1.getSalesAmount()));
        analysisItemResults.sort((o1, o2) -> Double.compare(o2.getTotalSales(), o1.getTotalSales()));
        return new AnalysisResult(analysisResults, analysisItemResults);
    }

    private Map<String, SalesSummaryByProductCode> getSalesSummaryByProductCode(List<SalesData> data) {
        return data.stream()
                .collect(Collectors.groupingBy(SalesData::getParentProductType,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            double amount = list.stream().mapToDouble(SalesData::getTotalAmount).sum();
                            int quantity = list.stream().mapToInt(SalesData::getOrderQuantity).sum();

                            double wholesalePrice = list.stream()
                                    .mapToDouble(salesData -> {
                                        return salesData.getOrderQuantity() *
                                                orderDataRepository.findByBrandCode(salesData.getBrandCode().split("/")[0])
                                                        .flatMap(orderList -> orderList.stream().findFirst())
                                                        .map(OrderData::getWholesalePrice)
                                                        .orElse(0);

                                    })
                                    .sum();

                            double stock = list.stream()
                                    .map(sd -> sd.getBrandCode().split("/")[0])
                                    .distinct()
                                    .mapToDouble(brandCode -> stockDataRepository.findByBrandCode(brandCode)
                                            .map(stockList -> stockList.stream()
                                                    .mapToDouble(StockData::getStock)
                                                    .sum())
                                            .orElse(0.0))
                                    .sum();

                            double stockWholesalePrice = list.stream()
                                    .mapToDouble(salesData -> {
                                        return orderDataRepository.findByBrandCode(salesData.getBrandCode().split("/")[0])
                                                .flatMap(orderList -> orderList.stream().findFirst())
                                                .map(OrderData::getWholesalePrice)
                                                .orElse(0)
                                                *
                                                stockDataRepository.findByBrandCode(salesData.getBrandCode().split("/")[0])
                                                        .flatMap(stockList -> stockList.stream().findFirst())
                                                        .map(StockData::getStock)
                                                        .orElse(0.0);
                                    })
                                    .sum();

                            return SalesSummaryByProductCode.builder()
                                    .amount(amount)
                                    .quantity(quantity)
                                    .wholesalePrice(wholesalePrice)
                                    .stock(stock)
                                    .stockWholesalePrice(stockWholesalePrice)
                                    .build();
                        })));
    }
}

