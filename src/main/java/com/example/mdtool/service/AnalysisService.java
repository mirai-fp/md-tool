package com.example.mdtool.service;

import com.example.mdtool.domain.*;
import com.example.mdtool.repository.OrderDataRepository;
import com.example.mdtool.repository.SaleDataRepository;
import com.example.mdtool.repository.StockDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private final CategoryMapper mapper;

    public AnalysisResult analyze(LocalDate startDate, LocalDate endDate) {
        List<SalesData> currentWeekData = saleDataRepository.findByDateRange(startDate, endDate);
        List<SalesData> previousWeekData = saleDataRepository.findByDateRange(startDate.minusWeeks(1), endDate.minusWeeks(1));
        List<SalesData> previousYearData = saleDataRepository.findByDateRange(startDate.minusYears(1), endDate.minusYears(1));
        List<SalesData> currentMonthData = saleDataRepository.findByDateRange(startDate.withDayOfMonth(1), endDate);
        List<SalesData> previousYearMonthData = saleDataRepository.findByDateRange(startDate.minusYears(1).withDayOfMonth(1), endDate.minusYears(1));
        List<SalesData> previous10WeekData = saleDataRepository.findByDateRange(startDate.minusWeeks(9), endDate.minusWeeks(1));

        // 子商品タイプごとに今週、先週、昨年、今月の集計
        Map<String, SalesSummaryByProductCode> currentWeekSalesSummaryMap = getSalesSummaryByProductCode(currentWeekData);
        Map<String, SalesSummaryByProductCode> previousWeekSalesSummaryMap = getSalesSummaryByProductCode(previousWeekData);
        Map<String, SalesSummaryByProductCode> previousYearSalesSummaryMap = getSalesSummaryByProductCode(previousYearData);
        Map<String, SalesSummaryByProductCode> currentMonthSalesSummaryMap = getSalesSummaryByProductCode(currentMonthData);
        Map<String, SalesSummaryByProductCode> previousYearMonthSalesSummaryMap = getSalesSummaryByProductCode(previousYearMonthData);

        // 結果まとめ
        // overall
        List<AnalysisOverallResult> analysisOverallResults = new ArrayList<>();
        for (String childProductType : currentWeekSalesSummaryMap.keySet()) {
            SalesSummaryByProductCode currentMonthSalesSummary = currentMonthSalesSummaryMap.get(childProductType);
            SalesSummaryByProductCode previousYearMonthSalesSummary = nonNull(previousYearMonthSalesSummaryMap.get(childProductType)) ? previousYearMonthSalesSummaryMap.get(childProductType) : SalesSummaryByProductCode.builder().build();
            analysisOverallResults.add(
                    AnalysisOverallResult.builder()
                            .parentProductType(mapper.map(childProductType))
                            .childProductType(childProductType)
                            .monthlyAmount(currentMonthSalesSummary.getAmount())
                            .previousMonthlyAmount(previousYearMonthSalesSummary.getAmount())
                            .previousMonthlyAmountRate(currentMonthSalesSummary.getAmount() / previousYearMonthSalesSummary.getAmount())
                            .previousMonthlyAmountDiff(currentMonthSalesSummary.getAmount() - previousYearMonthSalesSummary.getAmount())
                            .build()
            );
        }

        // PB別
        Map<String, AnalysisPBResult> pbResult = new HashMap<>();
        pbResult.put("プロパー", getPBResult("プロパー", currentWeekData, previousWeekData, currentMonthData));
        pbResult.put("セール", getPBResult("セール", currentWeekData, previousWeekData, currentMonthData));

        // PB別売上構成比
        double pbSales = pbResult.values().stream()
                .mapToDouble(AnalysisPBResult::getTotalSalesAmount)
                .sum();

        if (pbSales != 0) {
            pbResult.values().forEach(result -> {
                double rate = result.getTotalSalesAmount() / pbSales;
                result.setSalesAmountChangeRate(rate); // setRate は double型想定
            });
        }


        // タイプごとの分析結果
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
                    .parentProductType(mapper.map(childProductType))
                    .childProductType(childProductType)
                    .salesAmount(currentAmount)
                    .previousSalesAmount(previousAmount)
                    .salesAmountChangeRate(salesAmountChangeRate)
                    .salesCompositionRate(salesCompositionRate)
                    .quantityChangeRate(quantityChangeRate)
                    .quantity(currentQuantity)
                    .previousQuantity(previousQuantity)
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
                            .map(list -> list.stream()
                                    .filter(orderData -> {
                                        LocalDate deliveryDate = orderData.getDeliveryDate();
                                        return deliveryDate != null
                                                && !deliveryDate.isBefore(startDate.minusWeeks(10))
                                                && !deliveryDate.isAfter(endDate);
                                    })
                                    .mapToInt(OrderData::getOrderQuantity).sum())
                            .orElse(0)
                    );
                    result.setExpectedOrderAmount(orderDataList
                            .map(list -> list.stream()
                                    .filter(orderData -> {
                                        LocalDate deliveryDate = orderData.getDeliveryDate();
                                        return deliveryDate != null
                                                && deliveryDate.isAfter(endDate);
                                    })
                                    .mapToInt(OrderData::getOrderQuantity).sum())
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
                    result.setTotalSalesAmount3Week(previous10WeekData.stream()
                            .filter(data -> data.getBrandCode().equals(result.getBrandCode()))
                                    .filter(data -> !data.getOrderDate().isBefore(startDate.minusWeeks(2)))
                            .mapToInt(SalesData::getOrderQuantity)
                            .sum());
                    result.setTotalSalesAmount10Week(previous10WeekData.stream()
                            .filter(data -> data.getBrandCode().equals(result.getBrandCode()))
                            .mapToInt(SalesData::getOrderQuantity)
                            .sum());
                    result.setProfit(result.getTotalSales() - result.getTotalWholeSales());
                    return result;
                })
                .collect(Collectors.toList());


        //表で扱いやすい形式に変更
        List<String> order = List.of("OT", "KN", "CT", "BL", "OP", "SK", "PT", "SHOES", "GOODS", "システム未定義");
        // analysisResults.sort((o1, o2) -> Double.compare(o2.getMonthlyAmount(), o1.getMonthlyAmount()));
        // analysisOverallResults.sort((o1, o2) -> Double.compare(o2.getMonthlyAmount(), o1.getMonthlyAmount()));
        analysisItemResults.sort((o1, o2) -> Double.compare(o2.getTotalSales(), o1.getTotalSales()));

        Map<String, List<AnalysisOverallResult>> groupedOMonthlyResults = analysisOverallResults.stream()
                .collect(Collectors.groupingBy(AnalysisOverallResult::getParentProductType)); // 親商品タイプでグループ化

        Map<String, List<AnalysisProductTypeResult>> groupedOWeeklyResults = analysisResults.stream()
                .collect(Collectors.groupingBy(AnalysisProductTypeResult::getParentProductType));


        Map<String, List<AnalysisOverallResult>> sortedMonthlyResults = new LinkedHashMap<>();
        Map<String, List<AnalysisProductTypeResult>> sortedWeeklyResults = new LinkedHashMap<>();

        order.forEach(key -> {
            if (groupedOMonthlyResults.containsKey(key)) {
                sortedMonthlyResults.put(key, groupedOMonthlyResults.get(key));
            }
            if (groupedOWeeklyResults.containsKey(key)) {
                sortedWeeklyResults.put(key, groupedOWeeklyResults.get(key));
            }
        });


        // ~wkの表記
        LocalDate firstDayOfYear = LocalDate.of(startDate.getYear(), 1, 1);
        // 年初からの経過日数
        long days = ChronoUnit.DAYS.between(firstDayOfYear, startDate);

        return new AnalysisResult(sortedMonthlyResults, pbResult, sortedWeeklyResults, analysisItemResults, (int) (days / 7) + 1);
    }

    private Map<String, SalesSummaryByProductCode> getSalesSummaryByProductCode(List<SalesData> data) {
        return data.stream()
                .collect(Collectors.groupingBy(SalesData::getChildProductType,
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
                                    .parentProductType(list.stream().findFirst().orElse(new SalesData()).getParentProductType())
                                    .amount(amount)
                                    .quantity(quantity)
                                    .wholesalePrice(wholesalePrice)
                                    .stock(stock)
                                    .stockWholesalePrice(stockWholesalePrice)
                                    .build();
                        })));
    }

    private AnalysisPBResult getPBResult(String type, List<SalesData> currentWeekData, List<SalesData> previousWeekData, List<SalesData> currentMonthData) {
        // Pの結果抽出
        List<SalesData> currentWeekProperSalesData = currentWeekData.stream()
                .filter(d -> d.getPriceType().equals(type))
                .toList();
        List<SalesData> previousWeekProperSalesData = previousWeekData.stream()
                .filter(d -> d.getPriceType().equals(type))
                .toList();
        List<SalesData> currentMonthProperSalesData = currentMonthData.stream()
                .filter(d -> d.getPriceType().equals(type))
                .toList();

        // 売上抽出
        double totalSalesAmount = currentMonthProperSalesData.stream()
                .mapToDouble(SalesData::getTotalAmount)
                .sum();

        // 原価抽出
        double totalWholesaleAmount = currentMonthProperSalesData.stream()
                .mapToDouble(salesData -> {
                    return orderDataRepository.findByBrandCode(salesData.getBrandCode().split("/")[0])
                            .flatMap(orderList -> orderList.stream().findFirst()) // 1件目を取得
                            .map(orderData -> orderData.getWholesalePrice() * salesData.getOrderQuantity()) // wholesalePrice * quantity
                            .orElseGet(() -> {
                                System.out.println("データが見つかりませんでした: brandCode = " + salesData.getBrandCode());
                                return 0;
                            }); // 該当なしは0を返す
                }).sum();

        // 当週売上点数
        double totalQuantityCurrentWeek = currentWeekProperSalesData.stream()
                .mapToDouble(SalesData::getOrderQuantity)
                .sum();

        // 先週売上点数
        double totalQuantityPreviousWeek = previousWeekProperSalesData.stream()
                .mapToDouble(SalesData::getOrderQuantity)
                .sum();

        System.out.println("totalSalesAmount:" + totalSalesAmount);
        System.out.println("totalWholesaleAmount:" + totalWholesaleAmount);

        return  AnalysisPBResult.builder()
                .totalSalesAmount(totalSalesAmount)
                .totalWholeSales(totalWholesaleAmount)
                .profitRate((totalSalesAmount - totalWholesaleAmount) / totalSalesAmount * 100)
                .totalQuantityCurrentWeek(totalQuantityCurrentWeek)
                .totalQuantityPreviousWeek(totalQuantityPreviousWeek)
                .totalQuantityChangeRate(totalQuantityCurrentWeek / totalQuantityPreviousWeek * 100)
                .build();
    }
}

