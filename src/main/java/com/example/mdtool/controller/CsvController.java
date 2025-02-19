package com.example.mdtool.controller;

import com.example.mdtool.domain.AnalysisResult;
import com.example.mdtool.service.AnalysisService;
import com.example.mdtool.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

import static java.util.Objects.nonNull;

@Controller
public class CsvController {

    @Autowired
    private CsvService csvService;

    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/")
    public String index() {
        return "upload";
    }

    @GetMapping("/analysis")
    public String analysis(Model model, LocalDate startDate, LocalDate endDate) {
        AnalysisResult analyze = analysisService.analyze(nonNull(startDate) ? startDate : LocalDate.of(2025, 2, 13), nonNull(endDate) ? endDate : LocalDate.of(2025, 2, 19));
        model.addAttribute("record", analyze.getAnalysisProductTypeResult());
        model.addAttribute("itemRecord", analyze.getAnalysisItemResult());
        return "analysis";
    }

    @PostMapping("/uploadsales")
    public String uploadSalesCsv(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            return "upload";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "Shift_JIS"))) {
            csvService.processSalesCSV(file);
        } catch (IOException e) {
            return "upload";
        }

        return "result";
    }

    @PostMapping("/uploadstock")
    public String uploadStockCsv(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            return "upload";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "Shift_JIS"))) {
            csvService.processStockCSV(file);
        } catch (IOException e) {
            return "upload";
        }

        return "result";
    }

    @PostMapping("/uploadorder")
    public String uploadOrderCsv(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            return "upload";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "Shift_JIS"))) {
            csvService.processOrderCSV(file);
        } catch (IOException e) {
            return "upload";
        }

        return "result";
    }
}