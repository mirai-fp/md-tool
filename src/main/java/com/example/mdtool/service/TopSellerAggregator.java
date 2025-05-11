package com.example.mdtool.service;

import com.example.mdtool.domain.OrderData;
import com.example.mdtool.domain.SalesData;
import com.example.mdtool.domain.YearWeekSkuSummary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TopSellerAggregator {
    public List<YearWeekSkuSummary> aggregateSalesByYearWeekSku(List<SalesData> salesDataList, String targetBrandCode) {
        WeekFields weekFields = WeekFields.ISO; // 月曜始まり

        return salesDataList.stream()
                .filter(data -> targetBrandCode.equals(data.getBrandCode()))
                .collect(Collectors.groupingBy(data -> {
                    LocalDate date = data.getOrderDate();
                    int year = date.getYear();
                    int week = date.get(weekFields.weekOfWeekBasedYear());
                    String color = data.getColor();
                    String size = data.getSize();
                    return new YearWeekSkuKey(year, week, color, size);
                }))
                .entrySet().stream()
                .map(entry -> {
                    YearWeekSkuKey key = entry.getKey();
                    int totalQuantity = entry.getValue().stream()
                            .mapToInt(SalesData::getOrderQuantity)
                            .sum();
                    return new YearWeekSkuSummary(
                            key.year(),
                            key.week(),
                            key.color(),
                            key.size(),
                            totalQuantity
                    );
                })
                .sorted(Comparator
                        .comparing(YearWeekSkuSummary::getYear)
                        .thenComparing(YearWeekSkuSummary::getWeek)
                        .thenComparing(YearWeekSkuSummary::getColor)
                        .thenComparing(YearWeekSkuSummary::getSize))
                .collect(Collectors.toList());
    }

    public List<YearWeekSkuSummary> aggregateOrderByYearWeekSku(List<OrderData> orderDataList) {
        WeekFields weekFields = WeekFields.ISO; // 月曜始まり

        return orderDataList.stream()
                .collect(Collectors.groupingBy(data -> {
                    LocalDate date = data.getOrderDate();
                    int year = date.getYear();
                    int week = date.get(weekFields.weekOfWeekBasedYear());
                    String color = data.getColor();
                    String size = data.getSize();
                    return new YearWeekSkuKey(year, week, color, size);
                }))
                .entrySet().stream()
                .map(entry -> {
                    YearWeekSkuKey key = entry.getKey();
                    int totalQuantity = entry.getValue().stream()
                            .mapToInt(OrderData::getOrderQuantity)
                            .sum();
                    return new YearWeekSkuSummary(
                            key.year(),
                            key.week(),
                            key.color(),
                            key.size(),
                            totalQuantity
                    );
                })
                .sorted(Comparator
                        .comparing(YearWeekSkuSummary::getYear)
                        .thenComparing(YearWeekSkuSummary::getWeek)
                        .thenComparing(YearWeekSkuSummary::getColor)
                        .thenComparing(YearWeekSkuSummary::getSize))
                .collect(Collectors.toList());
    }

    // 内部用のキー保持クラス
        private record YearWeekSkuKey(int year, int week, String color, String size) {

        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof YearWeekSkuKey)) return false;
                YearWeekSkuKey that = (YearWeekSkuKey) o;
                return year == that.year && week == that.week &&
                        Objects.equals(color, that.color) &&
                        Objects.equals(size, that.size);
            }

    }
}
