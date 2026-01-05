package com.whatsthatclip.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyzeController {
    @PostMapping("/api/analyze")
    public String analyzeVideo () {
        return "Backend is working";
    }
}
