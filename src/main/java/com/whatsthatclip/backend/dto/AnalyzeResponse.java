package com.whatsthatclip.backend.dto;

public class AnalyzeResponse {
    private String title;
    private String type;
    private String year;
    private String message;

    public void setTitle(String title) {
        this.title=title;
    }

    public String getTitle () {
        return title;
    }

    public void setType (String type) {
        this.type=type;
    }

    public String getType () {
        return type;
    }

    public void setYear (String year) {
        this.year=year;
    }

    public String getYear () {
        return year;
    }

    public void setMessage (String message) {
        this.message=message;
    }

    public String getMessage () {
        return message;
    }
}
