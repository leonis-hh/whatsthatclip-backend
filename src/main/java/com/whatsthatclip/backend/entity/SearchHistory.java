package com.whatsthatclip.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table (name = "search_history")
public class SearchHistory {
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
