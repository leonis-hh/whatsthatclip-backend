package com.whatsthatclip.backend.tmdb;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TmdbTvSearchResponse {
    private List<TmdbTvResult> results;
}