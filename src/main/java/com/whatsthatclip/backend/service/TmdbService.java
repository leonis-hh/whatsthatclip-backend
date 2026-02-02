package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.tmdb.TmdbSearchResponse;
import com.whatsthatclip.backend.tmdb.TmdbTvSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TmdbService {
    @Value("${tmdb.api.key}")
    private String apiKey;
    private RestTemplate restTemplate = new RestTemplate();

    public TmdbSearchResponse searchMovie(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + encodedQuery;
        return restTemplate.getForObject(url, TmdbSearchResponse.class);
    }

    public TmdbTvSearchResponse searchTv(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/tv?api_key=" + apiKey + "&query=" + encodedQuery;
        return restTemplate.getForObject(url, TmdbTvSearchResponse.class);
    }



}
