package com.example.mdtool.service;

import com.example.mdtool.domain.*;
import com.example.mdtool.repository.FavoriteDataRepository;
import com.example.mdtool.repository.OrderDataRepository;
import com.example.mdtool.repository.SaleDataRepository;
import com.example.mdtool.repository.StockDataRepository;
import com.example.mdtool.util.WeekUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.mdtool.util.WeekUtil.getWeekOfYear;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    @Autowired
    private final SaleDataRepository saleDataRepository;

    @Autowired
    private final StockDataRepository stockDataRepository;

    @Autowired
    private final OrderDataRepository orderDataRepository;

    @Autowired
    private final FavoriteDataRepository favoriteRepository;

    @Autowired
    private final CategoryMapper mapper;

    @Autowired
    TopSellerAggregator aggregator;

    public AnalysisResult analyze(LocalDate startDate, LocalDate endDate, String brand) {
        List<SalesData> currentWeekData = !StringUtils.isEmpty(brand) ? saleDataRepository.findByDateRangeAndBrand(startDate, endDate, brand) : saleDataRepository.findByDateRange(startDate, endDate);
        List<SalesData> previousWeekData = !StringUtils.isEmpty(brand) ? saleDataRepository.findByDateRangeAndBrand(startDate.minusWeeks(1), endDate.minusWeeks(1), brand) : saleDataRepository.findByDateRange(startDate.minusWeeks(1), endDate.minusWeeks(1));
        List<SalesData> previousYearData = !StringUtils.isEmpty(brand) ? saleDataRepository.findByDateRangeAndBrand(startDate.minusYears(1), endDate.minusYears(1), brand) : saleDataRepository.findByDateRange(startDate.minusYears(1), endDate.minusYears(1));
        List<SalesData> currentMonthData = !StringUtils.isEmpty(brand) ? saleDataRepository.findByDateRangeAndBrand(startDate.withDayOfMonth(1), YearMonth.from(startDate).atEndOfMonth(), brand) : saleDataRepository.findByDateRange(startDate.withDayOfMonth(1), YearMonth.from(startDate).atEndOfMonth());
        List<SalesData> previousYearMonthData = !StringUtils.isEmpty(brand) ? saleDataRepository.findByDateRangeAndBrand(startDate.minusYears(1).withDayOfMonth(1), endDate.minusYears(1), brand) : saleDataRepository.findByDateRange(startDate.minusYears(1).withDayOfMonth(1), endDate.minusYears(1));
        List<SalesData> previous10WeekData = !StringUtils.isEmpty(brand) ? saleDataRepository.findByDateRangeAndBrand(startDate.minusWeeks(9), endDate.minusWeeks(1), brand) : saleDataRepository.findByDateRange(startDate.minusWeeks(9), endDate.minusWeeks(1));

        // 売上0点でも在庫があるものは今週データに含まれて欲しいので、在庫データから0点売上のデータを作る
        Optional<List<StockData>> currentStockData = stockDataRepository.findByDate(startDate);
        currentStockData.orElse(List.of()).parallelStream()
                .forEach(sd -> {
                    currentWeekData.add(SalesData.builder()
                            .brandCode(sd.getBrandCode())
                            .childProductType(sd.getParentProductType())
                            .orderQuantity(0)
                            .sellingPrice(0.0)
                            .totalAmount(0.0)
                            .productName(sd.getProductName())
                            .priceType("")
                            .build());
                });

        // 子商品タイプごとに今週、先週、昨年、今月の集計
        Map<String, SalesSummaryByProductCode> currentWeekSalesSummaryMap = getSalesSummaryByProductCode(currentWeekData, startDate);
        Map<String, SalesSummaryByProductCode> previousWeekSalesSummaryMap = getSalesSummaryByProductCode(previousWeekData, startDate);
        Map<String, SalesSummaryByProductCode> previousYearSalesSummaryMap = getSalesSummaryByProductCode(previousYearData, startDate);
        Map<String, SalesSummaryByProductCode> currentMonthSalesSummaryMap = getSalesSummaryByProductCode(currentMonthData, startDate);
        Map<String, SalesSummaryByProductCode> previousYearMonthSalesSummaryMap = getSalesSummaryByProductCode(previousYearMonthData, startDate);

        // 結果まとめ
        // overall
        List<AnalysisOverallResult> analysisOverallResults = new ArrayList<>();
        for (String childProductType : currentMonthSalesSummaryMap.keySet()) {
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
            double salesAmountChangeRate = previousAmount != 0 ? currentAmount / previousAmount * 100 : 0;
            double quantityChangeRate = previousQuantity != 0 ? currentQuantity/ previousQuantity * 100 : 0;

            // 売上構成比
            double totalCurrentAmount = currentWeekSalesSummaryMap.values().stream()
                    .mapToDouble(SalesSummaryByProductCode::getAmount)
                    .sum();
            double salesCompositionRate = totalCurrentAmount != 0 ? (currentAmount / totalCurrentAmount) * 100 : 0;

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
        List<AnalysisItemResult> analysisItemResults = currentWeekData.parallelStream()
                .collect(Collectors.groupingBy(
                        SalesData::getBrandCode,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    String productName = list.stream().map(SalesData::getProductName).findFirst().orElse("Unknown");
                                    double totalSales = list.stream().mapToDouble(SalesData::getTotalAmount).sum();
                                    double totalQuantity = list.stream().mapToInt(SalesData::getOrderQuantity).sum();
                                    double retailPrice = list.stream().mapToDouble(SalesData::getSellingPrice).max().orElse(0.0);
                                    return new SalesSummary(productName, totalSales, totalQuantity, retailPrice);
                                }
                        )
                ))
                .entrySet().parallelStream()
                .map(entry -> {
                    AnalysisItemResult result = new AnalysisItemResult();
                    result.setBrandCode(entry.getKey());       // BrandCode
                    result.setProductName(entry.getValue().getProductName());  // ProductName
                    result.setTotalSales(entry.getValue().getTotalSales());
                    result.setTotalSalesAmount(entry.getValue().getTotalQuantity());
                    result.setStock(stockDataRepository.findByBrandCodeAndDate(entry.getKey(), startDate)
                            .map(stockDataList -> stockDataList.stream()
                                    .mapToDouble(StockData::getStock)
                                    .sum()
                            )
                            .orElse(0.0)
                    );
                    Optional<List<OrderData>> orderDataList = orderDataRepository.findByBrandCode(result.getBrandCode().split("/")[0]);
                    result.setOrderAmount(orderDataList
                            .map(list -> list.parallelStream()
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
                            .map(list -> list.parallelStream()
                                    .filter(orderData -> {
                                        LocalDate deliveryDate = orderData.getDeliveryDate();
                                        return deliveryDate != null
                                                && deliveryDate.isAfter(endDate);
                                    })
                                    .mapToInt(OrderData::getOrderQuantity).sum())
                            .orElse(0)
                    );

                    result.setWholesalePrice(orderDataList
                            .map(odl -> odl.stream()
                                    .findFirst()
                                    .map(OrderData::getWholesalePrice)
                                    .orElse(0)
                            )
                            .orElse(0));

                    result.setTotalWholeSales(orderDataList
                            .map(odl -> odl.stream()
                                    .findFirst()
                                    .map(OrderData::getWholesalePrice)
                                    .orElse(0)
                            )
                            .orElse(0) * result.getTotalSalesAmount()
                    );
                    result.setRetailPrice(orderDataList
                            .map(odl -> odl.stream()
                                    .findFirst()
                                    .map(OrderData::getSellingPrice)
                                    .orElse(0)
                            )
                            .orElse(0));
                    result.setTotalSalesAmount1WeekAgo(previous10WeekData.stream()
                            .filter(data -> data.getBrandCode().equals(result.getBrandCode()))
                                    .filter(data -> !data.getOrderDate().isBefore(startDate.minusWeeks(1))
                                    && !data.getOrderDate().isAfter(endDate.minusWeeks(1)))
                            .mapToInt(SalesData::getOrderQuantity)
                            .sum());
                    result.setTotalSalesAmount2WeekAgo(previous10WeekData.stream()
                            .filter(data -> data.getBrandCode().equals(result.getBrandCode()))
                                    .filter(data -> !data.getOrderDate().isBefore(startDate.minusWeeks(1))
                                            && !data.getOrderDate().isAfter(endDate.minusWeeks(1)))
                            .mapToInt(SalesData::getOrderQuantity)
                            .sum());
                    result.setProfit(result.getTotalSales() - result.getTotalWholeSales());

                    Integer favoriteCount = favoriteRepository.findFavoriteDataByBrandCodeAndDate(entry.getKey(), startDate).orElse(new FavoriteData()).getFavoriteCount();
                    result.setFavoriteCount(nonNull(favoriteCount) ? favoriteCount : 0);
                    Integer favoriteCount1WeekAgo = favoriteRepository.findFavoriteDataByBrandCodeAndDate(entry.getKey(), startDate.minusWeeks(1)).orElse(new FavoriteData()).getFavoriteCount();
                    result.setFavoriteCount1WeekAgo(nonNull(favoriteCount1WeekAgo) ? favoriteCount1WeekAgo : 0);

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

        return new AnalysisResult(sortedMonthlyResults, pbResult, sortedWeeklyResults, analysisItemResults, getWeekOfYear(startDate));
    }

    private Map<String, SalesSummaryByProductCode> getSalesSummaryByProductCode(List<SalesData> data, LocalDate startDate) {
        // 1. 全ブランドコードを一度だけ抽出
        Set<String> allBrands = data.parallelStream()
                .map(sd -> sd.getBrandCode().split("/")[0])
                .collect(Collectors.toSet());

        // 2. 一括で wholesalePrice をフェッチして Map<ブランド, 卸売単価>、在庫合計をフェッチして Map<ブランド, 在庫合計>
        long start = System.currentTimeMillis();
        Map<String, Integer> wholesaleMap = new ConcurrentHashMap<>();
        Map<String, Double> stockMap = new ConcurrentHashMap<>();
        allBrands.parallelStream().forEach(brand -> {
            Integer wp = orderDataRepository.findByBrandCode(brand)
                    .flatMap(list -> list.stream().findFirst())
                    .map(OrderData::getWholesalePrice)
                    .orElse(0);
            wholesaleMap.put(brand, wp);

            double stockTotal = stockDataRepository.findByBrandCodeAndDate(brand, startDate)
                    .map(list -> list.stream().mapToDouble(StockData::getStock).sum())
                    .orElse(0.0);
            stockMap.put(brand, stockTotal);
        });
        long elapsed = System.currentTimeMillis() - start;
        log.info("原価data SQL実行時間={}ms", elapsed);

        return data.stream()
                .collect(Collectors.groupingBy(SalesData::getChildProductType,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            double amount = list.stream().mapToDouble(SalesData::getTotalAmount).sum();
                            int quantity = list.stream().mapToInt(SalesData::getOrderQuantity).sum();

                            double wholesalePrice = list.parallelStream()
                                    .mapToDouble(salesData -> {
                                        return salesData.getOrderQuantity() *
                                                wholesaleMap.getOrDefault(salesData.getBrandCode().split("/")[0], 0);

                                    })
                                    .sum();

                            double stock = list.parallelStream()
                                    .map(sd -> sd.getBrandCode().split("/")[0])
                                    .distinct()
                                    .mapToDouble(brandCode -> stockDataRepository.findByBrandCodeAndDate(brandCode, startDate)
                                            .map(stockList -> stockList.stream()
                                                    .mapToDouble(StockData::getStock)
                                                    .sum())
                                            .orElse(0.0))
                                    .sum();

                            double stockWholesalePrice = list.parallelStream()
                                    .map(SalesData::getBrandCode)
                                    .distinct()
                                    .mapToDouble(b -> {
                                        return  wholesaleMap.getOrDefault(b.split("/")[0], 0)
                                                *
                                                stockMap.getOrDefault(b.split("/")[0], 0.0);
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

        return  AnalysisPBResult.builder()
                .totalSalesAmount(totalSalesAmount)
                .totalWholeSales(totalWholesaleAmount)
                .profitRate((totalSalesAmount - totalWholesaleAmount) / totalSalesAmount * 100)
                .totalQuantityCurrentWeek(totalQuantityCurrentWeek)
                .totalQuantityPreviousWeek(totalQuantityPreviousWeek)
                .totalQuantityChangeRate(totalQuantityCurrentWeek / totalQuantityPreviousWeek * 100)
                .build();
    }

    public void analyzeTopSeller(String brandCode, Model model) {
        List<SalesData> salesList = saleDataRepository.findSalesDataByBrandCode(brandCode);
        List<YearWeekSkuSummary> summaryList = aggregator.aggregateSalesByYearWeekSku(salesList, brandCode);

        // 1. 週の一覧（順序あり）
        Set<String> allYearWeeksSet = summaryList.stream()
                .map(YearWeekSkuSummary::getYearWeek)
                .collect(Collectors.toCollection(TreeSet::new));
        List<String> allYearWeeks = new ArrayList<>(allYearWeeksSet);

        // TODO: 本来は処理でデータを足したいが、一旦10週分足す
        WeekUtil.addWeekData(allYearWeeks);

        // 2. 年月ごとの週番号（ヘッダー用）
        Map<String, List<String>> monthToWeeksMap = new LinkedHashMap<>();
        for (String yearWeek : allYearWeeks) {
            String[] parts = yearWeek.split("-W");
            int weekNum = Integer.parseInt(parts[1]);
            YearMonth ym = WeekUtil.getYearMonthFromWeek(Integer.parseInt(parts[0]), weekNum); // 例: 2024年10月
            String label = ym.getYear() + "年" + ym.getMonthValue() + "月";

            monthToWeeksMap.computeIfAbsent(label, k -> new ArrayList<>()).add(String.valueOf(weekNum));
        }

        List<MonthWeeks> monthList = monthToWeeksMap.entrySet().stream()
                .map(entry -> new MonthWeeks(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 3. カラー×サイズごとの売上マップ化
        Map<String, SalesTableRow> rowMap = new LinkedHashMap<>();
        for (YearWeekSkuSummary summary : summaryList) {
            String key = summary.getColor() + "-" + summary.getSize();
            rowMap.putIfAbsent(key, new SalesTableRow(summary.getColor(), summary.getSize(), new HashMap<>()));
            rowMap.get(key).getWeeklySales().put(summary.getYearWeek(), summary.getTotalQuantity());
        }

        List<SalesTableRow> salesTable = new ArrayList<>(rowMap.values());

        // 追加: 合計値の計算
        Map<String, Integer> weeklySalesSum = new HashMap<>();
        for (SalesTableRow row : salesTable) {
            Map<String, Integer> sales = row.getWeeklySales();
            if (sales != null) {
                for (Map.Entry<String, Integer> entry : sales.entrySet()) {
                    String week = entry.getKey();
                    Integer count = entry.getValue();
                    if (count != null) {
                        weeklySalesSum.merge(week, count, Integer::sum);
                    }
                }
            }
        }

        // 5. アイテム情報取得
        Optional<List<OrderData>> orderData = orderDataRepository.findByBrandCode(brandCode.split("/")[0]);

        // 4. Thymeleafに渡す
        model.addAttribute("itemName", salesList.get(0).getProductName());
        model.addAttribute("brandCode", brandCode);
        model.addAttribute("sellingPrice", orderData.get().get(0).getSellingPrice());
        model.addAttribute("wholesalePrice", orderData.get().get(0).getWholesalePrice());
        model.addAttribute("allYearWeeks", allYearWeeks);
        model.addAttribute("monthList", monthList);
        model.addAttribute("salesTable", salesTable);
        model.addAttribute("salesTableSum", weeklySalesSum);

        // お気に入りも同様に処理
        List<FavoriteData> favoriteData = favoriteRepository.findFavoriteDataByBrandCode(brandCode);
        Map<String, List<FavoriteData>> grouped = favoriteData.stream()
                .collect(Collectors.groupingBy(FavoriteData::getProductName));

        List<FavoriteTableRow> favoriteTable = new ArrayList<>();

        for (Map.Entry<String, List<FavoriteData>> entry : grouped.entrySet()) {
            List<FavoriteData> dataList = entry.getValue();

            FavoriteTableRow row = new FavoriteTableRow(new HashMap<>());
            for (FavoriteData fd : dataList) {
                String week = WeekUtil.getYearWeek(fd.getDate()); // e.g. "2025-W14"
                row.getWeeklyFavorite().merge(week, fd.getFavoriteCount(), Integer::sum);
            }

            favoriteTable.add(row);
        }
        model.addAttribute("favoriteTable", !favoriteTable.isEmpty() ? favoriteTable.get(0) : Map.of());

        // 在庫も同様に処理
        Optional<List<StockData>> stockData = stockDataRepository.findByBrandCode(brandCode);
        Map<String, StockTableRow> stockRowMap = new LinkedHashMap<>();
        for (StockData data : stockData.orElse(List.of())) {
            String key = data.getColor() + "-" + data.getSize();
            stockRowMap.putIfAbsent(key, new StockTableRow(data.getColor(), data.getSize(), new HashMap<>()));
            stockRowMap.get(key).getWeeklyStock().put(WeekUtil.getYearWeek(data.getDate()), data.getStock());
        }
        List<StockTableRow> stockTable = new ArrayList<>(stockRowMap.values());
        model.addAttribute("stockTable", stockTable);

        // 追加: 合計値の計算
        Map<String, Integer> weeklyStockSum = new HashMap<>();
        for (StockTableRow row : stockTable) {
            Map<String, Integer> sales = row.getWeeklyStock();
            if (sales != null) {
                for (Map.Entry<String, Integer> entry : sales.entrySet()) {
                    String week = entry.getKey();
                    Integer count = entry.getValue();
                    if (count != null) {
                        weeklyStockSum.merge(week, count, Integer::sum);
                    }
                }
            }
        }
        model.addAttribute("stockTableSum", weeklyStockSum);

        // 発注残も同様に処理
        Map<String, OrderTableRow> orderRowMap = new LinkedHashMap<>();
        List<YearWeekSkuSummary> orderSummaryList = aggregator.aggregateOrderByYearWeekSku(orderData.orElse(List.of()));
        for (YearWeekSkuSummary summary : orderSummaryList) {
            String key = summary.getColor() + "-" + summary.getSize();
            orderRowMap.putIfAbsent(key, new OrderTableRow(summary.getColor(), summary.getSize(), new HashMap<>()));
            orderRowMap.get(key).getWeeklyOrder().put(summary.getYearWeek(), summary.getTotalQuantity());
        }
        List<OrderTableRow> orderTable = new ArrayList<>(orderRowMap.values());
        model.addAttribute("orderTable", orderTable);

        // 追加: 合計値の計算
        Map<String, Integer> weeklyOrderSum = new HashMap<>();
        for (OrderTableRow row : orderTable) {
            Map<String, Integer> sales = row.getWeeklyOrder();
            if (sales != null) {
                for (Map.Entry<String, Integer> entry : sales.entrySet()) {
                    String week = entry.getKey();
                    Integer count = entry.getValue();
                    if (count != null) {
                        weeklyOrderSum.merge(week, count, Integer::sum);
                    }
                }
            }
        }
        model.addAttribute("orderTableSum", weeklyOrderSum);

    }

}

