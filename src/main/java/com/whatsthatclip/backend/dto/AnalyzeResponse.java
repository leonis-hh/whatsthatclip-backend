package com.whatsthatclip.backend.dto;

import lombok.Data;

@Data
public class AnalyzeResponse {
    private String title;
    private String type;
    private String year;
    private String message;
    private String overview;
    private String posterUrl;

}
