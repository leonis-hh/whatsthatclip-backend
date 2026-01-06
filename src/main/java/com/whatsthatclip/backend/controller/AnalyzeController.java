package com.whatsthatclip.backend.controller;

import com.whatsthatclip.backend.dto.AnalyzeRequest;
import com.whatsthatclip.backend.dto.AnalyzeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyzeController {
    @PostMapping("/api/analyze")
    public AnalyzeResponse analyzeVideo (@RequestBody AnalyzeRequest request) {
        AnalyzeResponse response = new AnalyzeResponse();
        response.setTitle("Title");
        response.setType("Movie/TV Show");
        response.setYear("2026");
        response.setMessage("Recieved URL: " + request.getVideoUrl() );
        return response;
    }


}
