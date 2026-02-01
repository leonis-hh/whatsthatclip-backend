package com.whatsthatclip.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table (name = "favorite")
public class Favorite {
    public Favorite (String title, String type, String year, String overview, String posterUrl, LocalDateTime savedAt, User user) {
        this.title = title;
        this.type = type;
        this.year = year;
        this.overview = overview;
        this.posterUrl = posterUrl;
        this.savedAt = savedAt;
        this.user=user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String type;
    private String year;
    private LocalDateTime savedAt;
    @Column(length = 2000)
    private String overview;
    private String posterUrl;
    @ManyToOne
    @JsonIgnore
    private User user;

}