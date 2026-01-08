package com.whatsthatclip.backend.controller;

import com.whatsthatclip.backend.dto.AnalyzeRequest;
import com.whatsthatclip.backend.dto.AnalyzeResponse;
import com.whatsthatclip.backend.service.AnalyzeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class AnalyzeController {
    private AnalyzeService service;

    public AnalyzeController (AnalyzeService service) {
        this.service=service;
    }
    @PostMapping("/api/analyze")
    public AnalyzeResponse analyzeVideo (@RequestBody AnalyzeRequest request) {
        return service.analyze(request);
    }


}
