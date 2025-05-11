package com.example.mdtool.controller;

import com.example.mdtool.config.AccountPermissionProperties;
import com.example.mdtool.config.SecurityConfig;
import com.example.mdtool.domain.AnalysisResult;
import com.example.mdtool.domain.MdUserData;
import com.example.mdtool.repository.MdUserDataRepository;
import com.example.mdtool.service.AnalysisService;
import com.example.mdtool.service.CsvService;
import com.example.mdtool.util.WeekUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

import static java.util.Objects.nonNull;

@Controller
@AllArgsConstructor
public class CsvController {

    private CsvService csvService;

    private AnalysisService analysisService;

    private AccountPermissionProperties accountPermissionProperties;

    private MdUserDataRepository mdUserDataRepository;

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index(Model model) {
        // プルダウンの日付リスト
        LocalDate today = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        List<String> weekList = new ArrayList<>();

        for (int i = -15; i <= 10; i++) {
            LocalDate target = today.plusWeeks(i);
            weekList.add(WeekUtil.getYearWeek(target));
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        final int[] count = {0};
        List<WeekOption> weekOptions = weekList.stream()
                .map(w -> {
                    count[0]++;
                    String[] parts = w.split("-W");
                    int year = Integer.parseInt(parts[0]);
                    int weekNum = Integer.parseInt(parts[1]);

                    // その週の開始、終了日
                    LocalDate startDate = WeekUtil.getWeekStartDate(w);
                    LocalDate endDate = WeekUtil.getWeekEndDate(w);

                    // ラベルを組み立て
                    String label = String.format(
                            "%04d年%02d週末時点 (~" + endDate.format(fmt) + ")",
                            year, weekNum
                    );

                    String favLabel = String.format(
                            "%04d年%02d週増加数 (" + startDate.format(fmt) + "~" + endDate.format(fmt) +  ")",
                            year, weekNum
                    );

                    return new WeekOption(w, count[0] == 15, label, favLabel);
                })
                .toList();

        model.addAttribute("weekOptions", weekOptions);

        return "upload";
    }

    @GetMapping("/analysis")
    public String analysis(Model model, @RequestParam("brand")String brandName, @RequestParam("week")String targetWeek) {
        // 許可ブランドリストの取得
        String account = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("brandList", accountPermissionProperties.getAllowedBrands(account));

        // プルダウンの日付リスト
        LocalDate today = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        List<String> weekList = new ArrayList<>();

        for (int i = -20; i <= 10; i++) {
            LocalDate target = today.plusWeeks(i);
            weekList.add(WeekUtil.getYearWeek(target));
        }

        final int[] count = {0};
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        List<WeekOption> weekOptions = weekList.stream()
                .map(w -> {
                    count[0]++;
                    String[] parts = w.split("-W");
                    int year = Integer.parseInt(parts[0]);
                    int weekNum = Integer.parseInt(parts[1]);

                    // その週の開始日
                    LocalDate startDate = WeekUtil.getWeekStartDate(w);


                    // ラベルを組み立て
                    String label = String.format(
                            "%04d年%02d週 (" + startDate.format(fmt) + "~)",
                            year, weekNum
                    );

                    return new WeekOption(w, count[0] == 20, label);
                })
                .toList();

        model.addAttribute("weekOptions", weekOptions);

        // 値が設定されていたら処理実施
        if (!StringUtils.isEmpty(targetWeek)) {
            AnalysisResult analyze = analysisService.analyze(WeekUtil.getWeekStartDate(targetWeek), WeekUtil.getWeekEndDate(targetWeek), brandName);

            model.addAttribute("overallRecord", analyze.getAnalysisOverallResult());
            model.addAttribute("collectRecord", analyze);
            model.addAttribute("pbRecord", analyze.getAnalysisPBResult());
            model.addAttribute("record", analyze.getAnalysisProductTypeResult());
            model.addAttribute("itemRecord", analyze.getAnalysisItemResult());

            // 何月集計かのラベル
            model.addAttribute("month", WeekUtil.getWeekStartDate(targetWeek).getMonthValue());
        }
        else {
            // 過去日付で処理だけ通す
            AnalysisResult analyze = analysisService.analyze(LocalDate.of(1999, 11, 11), LocalDate.of(1999, 11, 11), brandName);

            model.addAttribute("overallRecord", analyze.getAnalysisOverallResult());
            model.addAttribute("collectRecord", analyze);
            model.addAttribute("pbRecord", analyze.getAnalysisPBResult());
            model.addAttribute("record", analyze.getAnalysisProductTypeResult());
            model.addAttribute("itemRecord", analyze.getAnalysisItemResult());
        }

        // 選択値も model で返却
        model.addAttribute("selectedBrand", brandName);
        model.addAttribute("selectedWeek", targetWeek);

        return "analysis";
    }

    @GetMapping("/item-analysis")
    public String itemAnalysis(Model model, String id) {
        analysisService.analyzeTopSeller(id, model);
        return "itemAnalysis";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // login.html を返す
    }

    @PostMapping("/uploadsales")
    public String uploadSalesCsv(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            return "upload";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "Shift_JIS"))) {
            model.addAttribute("result", csvService.processSalesCSV(file));
        } catch (IOException e) {
            return "upload";
        }

        return "result";
    }

    @PostMapping("/uploadstock")
    public String uploadStockCsv(@RequestParam("file") MultipartFile file, Model model, @RequestParam("stockweek") String week) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            return "upload";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "Shift_JIS"))) {
            model.addAttribute("result", csvService.processStockCSV(file, WeekUtil.getWeekStartDate(week)));
        } catch (IOException e) {
            return "upload";
        }

        return "result";
    }

    @PostMapping("/uploadorder")
    public String uploadOrderCsv(@RequestParam("files") List<MultipartFile> files, Model model) {
        if (CollectionUtils.isEmpty(files)) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            return "upload";
        }

        for (MultipartFile file: files) {
            System.out.println("register start. file:" + file.getOriginalFilename());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "Shift_JIS"))) {
                model.addAttribute("result", csvService.processOrderCSV(file));
            } catch (IOException e) {
                return "upload";
            }
        }

        return "result";
    }

    @PostMapping("/uploadfav")
    public String uploadFavCsv(@RequestParam("file") MultipartFile file, @RequestParam("favweek") String week, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            return "upload";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "Shift_JIS"))) {
            model.addAttribute("result", csvService.processFavCSV(file, WeekUtil.getWeekStartDate(week)));
        } catch (IOException e) {
            return "upload";
        }

        return "result";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/register_user")
    public String registerUser(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("display_name") String displayName, @RequestParam("roles") String... roles) {
        MdUserData mdUserData = MdUserData.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .displayName(displayName)
                .roles(new HashSet<>(Arrays.asList(roles)))
                .build();
        mdUserDataRepository.save(mdUserData);
        return "register";
    }
}

record WeekOption(String code, boolean defaultFlag, String... label) {}
