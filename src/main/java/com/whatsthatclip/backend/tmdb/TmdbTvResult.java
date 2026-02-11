package com.whatsthatclip.backend.tmdb;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TmdbTvResult {
    private Integer id;
    private String name;
    private String overview;
    private String first_air_date;
    private String poster_path;
    private Double popularity;
}
