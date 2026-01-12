package com.whatsthatclip.backend.tmdb;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TmdbSearchResponse {
    private List<TmdbMovieResult> results;
}
