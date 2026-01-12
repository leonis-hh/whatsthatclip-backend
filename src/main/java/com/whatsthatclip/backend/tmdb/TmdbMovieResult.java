package com.whatsthatclip.backend.tmdb;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TmdbMovieResult {
    private String title;
    private String overview;
    private String release_date;
    private String poster_path;
    private Double popularity;
}