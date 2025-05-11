package com.example.mdtool.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class WeekUtil {

    /**
     * 年初から「月曜～日曜」を一単位として数えた週番号を返す。
     * 例えば
     *   年初の1/1～最初の日曜 → 週1
     *   次の月曜～日曜           → 週2
     *   以降、同様に数える
     */
    public static int getWeekOfYear(LocalDate date) {
        // 1月1日の週を「週1」とするための境界：年初の最初の月曜日
        LocalDate firstMonday = LocalDate.of(date.getYear(), 1, 1)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        // date がその月曜日より前（年初1/1～最初の日曜）の場合
        if (date.isBefore(firstMonday)) {
            return 1;
        }

        // 最初の月曜日以降の日数差
        long daysSinceFirstMonday = ChronoUnit.DAYS.between(firstMonday, date);

        // 週番号は「週1（年初1/1～日曜）」を起点に、
        //     floor(daysSinceFirstMonday / 7) + 2
        // を返す
        return (int)(daysSinceFirstMonday / 7) + 2;
    }

    /**
     * "YYYY-Www" 形式の週番号から、その週の開始日（カスタム：週1は年初1/1、それ以降は月曜日）を返します。
     */
    public static LocalDate getWeekStartDate(String yearWeek) {
        String[] parts = yearWeek.split("-W");
        int year  = Integer.parseInt(parts[0]);
        int week  = Integer.parseInt(parts[1]);

        LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
        // 年の最初の月曜日
        LocalDate firstMonday = firstDayOfYear.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        if (week <= 1) {
            // 週1 は年初の日付を開始日とする
            return firstDayOfYear;
        }
        // 週2 → firstMonday, 週3 → firstMonday+1週 ･･･
        return firstMonday.plusWeeks(week - 2);
    }

    /**
     * "YYYY-Www" 形式の週番号から、その週の終了日（日曜日、年末を超える場合は12/31）を返します。
     */
    public static LocalDate getWeekEndDate(String yearWeek) {
        LocalDate start = getWeekStartDate(yearWeek);
        LocalDate end   = start.plusDays(6);

        // 年末の日付
        int year = Integer.parseInt(yearWeek.split("-W")[0]);
        LocalDate lastDayOfYear = LocalDate.of(year, 12, 31);

        // 年末を超えた場合は12/31を返す
        return end.isAfter(lastDayOfYear) ? lastDayOfYear : end;
    }

    /** "yyyy-Www" 形式を受け取って、その週の開始日(週1は1/1、それ以降は月曜)を返す */
    private static LocalDate getWeekStartDate(int year, int week) {
        LocalDate firstDay = LocalDate.of(year, 1, 1);
        LocalDate firstMonday = firstDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        if (week <= 1) {
            return firstDay;
        }
        return firstMonday.plusWeeks(week - 2);
    }

    /** allYearWeeks の最後の要素から10週先までを追加する */
    public static void addWeekData(List<String> allYearWeeks) {
        String last = allYearWeeks.get(allYearWeeks.size() - 1); // e.g. "2025-W17"
        String[] p = last.split("-W");
        int year = Integer.parseInt(p[0]);
        int week = Integer.parseInt(p[1]);

        LocalDate start = getWeekStartDate(year, week);
        for (int i = 1; i <= 10; i++) {
            LocalDate nextStart = start.plusWeeks(i);
            int y = nextStart.getYear();
            int w = getWeekOfYear(nextStart);
            allYearWeeks.add(String.format("%04d-W%02d", y, w));
        }
    }

    /** 独自週→YearMonth（開始日の年月）を返す */
    public static YearMonth getYearMonthFromWeek(int year, int week) {
        LocalDate start = getWeekStartDate(year, week);
        return YearMonth.from(start);
    }

    /** LocalDate→"yyyy-Www" 形式の独自週番号を返す */
    public static String getYearWeek(LocalDate date) {
        int y = date.getYear();
        int w = getWeekOfYear(date);
        return String.format("%04d-W%02d", y, w);
    }
}