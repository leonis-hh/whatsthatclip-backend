package com.whatsthatclip.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table (name = "search_history")
public class SearchHistory {
    public SearchHistory(String videoUrl, String title, String type, String year, String overview, String posterUrl, LocalDateTime searchedAt) {
        this.videoUrl = videoUrl;
        this.title = title;
        this.type = type;
        this.year = year;
        this.overview = overview;
        this.posterUrl = posterUrl;
        this.searchedAt = searchedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String videoUrl;
    private String title;
    private String type;
    private String year;
    private LocalDateTime searchedAt;
    private String overview;
    private String posterUrl;

}
