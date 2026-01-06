package com.whatsthatclip.backend.dto;

public class AnalyzeRequest {
    private String videoUrl;

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoUrl () {
        return videoUrl;
    }
}
