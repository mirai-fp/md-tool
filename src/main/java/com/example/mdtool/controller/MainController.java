package com.example.mdtool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/hoge")
    public String home(Model model) {
        model.addAttribute("message", "Hello, Spring Boot with Thymeleaf!");
        return "index"; // "index" はテンプレートファイル名（拡張子なし）
    }
}